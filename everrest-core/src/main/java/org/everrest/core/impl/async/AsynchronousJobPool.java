/*
 * Copyright (c) 2012-2021 Codenvy, S.A.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
package org.everrest.core.impl.async;

import static java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import jakarta.annotation.PreDestroy;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import org.everrest.core.ApplicationContext;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.impl.ContainerRequest;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.resource.ResourceMethodDescriptor;
import org.everrest.core.tools.EmptyInputStream;
import org.slf4j.LoggerFactory;

/**
 * Pool of asynchronous jobs.
 *
 * @author andrew00x
 */
@Provider
public class AsynchronousJobPool implements ContextResolver<AsynchronousJobPool> {
  /** Logger. */
  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AsynchronousJobPool.class);

  protected final String asynchronousServicePath;
  /** When timeout (in minutes) reached then an asynchronous job may be removed from the pool. */
  protected final int jobTimeout;
  /** Max cache size. */
  protected final int maxCacheSize;
  /** Maximum number of task in queue. */
  protected final int maxQueueSize;
  /** Number of threads to serve asynchronous jobs. */
  protected final int threadPoolSize;

  private final ExecutorService pool;
  private final Map<Long, AsynchronousJob> jobs;
  private final CopyOnWriteArrayList<AsynchronousJobListener> jobListeners;

  private AsynchronousFutureFactory asynchronousFutureFactory;

  public AsynchronousJobPool(EverrestConfiguration config) {
    if (config == null) {
      config = new EverrestConfiguration();
    }

    this.asynchronousServicePath = config.getAsynchronousServicePath();
    this.maxCacheSize = config.getAsynchronousCacheSize();
    this.jobTimeout = config.getAsynchronousJobTimeout();
    this.maxQueueSize = config.getAsynchronousQueueSize();
    this.threadPoolSize = config.getAsynchronousPoolSize();

    this.pool = makeExecutorService();

    this.jobs =
        Collections.synchronizedMap(
            new LinkedHashMap<Long, AsynchronousJob>() {
              @Override
              protected boolean removeEldestEntry(Map.Entry<Long, AsynchronousJob> eldest) {
                AsynchronousJob job = eldest.getValue();
                if (size() > maxCacheSize || job.getExpirationDate() < System.currentTimeMillis()) {
                  job.cancel();
                  return true;
                }
                return false;
              }
            });

    this.jobListeners = new CopyOnWriteArrayList<>();

    setAsynchronousFutureFactory(new AsynchronousFutureFactory());
  }

  public String getAsynchronousServicePath() {
    return asynchronousServicePath;
  }

  public int getMaxCacheSize() {
    return maxCacheSize;
  }

  public int getMaxQueueSize() {
    return maxQueueSize;
  }

  public int getThreadPoolSize() {
    return threadPoolSize;
  }

  public int getJobTimeout() {
    return jobTimeout;
  }

  void setAsynchronousFutureFactory(AsynchronousFutureFactory asynchronousFutureFactory) {
    this.asynchronousFutureFactory = asynchronousFutureFactory;
  }

  protected ExecutorService makeExecutorService() {
    return new ThreadPoolExecutor(
        threadPoolSize,
        threadPoolSize,
        0L,
        MILLISECONDS,
        new LinkedBlockingQueue<>(maxQueueSize),
        new ThreadFactoryBuilder()
            .setNameFormat("everrest.AsynchronousJobPool-%d")
            .setDaemon(true)
            .build(),
        new ManyJobsPolicy(new AbortPolicy()));
  }

  @Override
  public AsynchronousJobPool getContext(Class<?> type) {
    return this;
  }

  /**
   * @param resource object that contains resource method
   * @param resourceMethod resource or sub-resource method to invoke
   * @param params method parameters
   * @return asynchronous job
   * @throws AsynchronousJobRejectedException if this task cannot be added to pool
   */
  public final AsynchronousJob addJob(
      Object resource, ResourceMethodDescriptor resourceMethod, Object[] params)
      throws AsynchronousJobRejectedException {
    final long expirationDate = System.currentTimeMillis() + MINUTES.toMillis(jobTimeout);
    final Callable<Object> callable = newCallable(resource, resourceMethod.getMethod(), params);
    final AsynchronousFuture job =
        asynchronousFutureFactory.createAsynchronousFuture(
            callable, expirationDate, resourceMethod, jobListeners);
    job.setJobURI(getAsynchronousJobUriBuilder(job).build().toString());

    final ApplicationContext context = ApplicationContext.getCurrent();
    final ContainerRequest request =
        createRequestCopy(context.getContainerRequest(), context.getSecurityContext());

    job.getContext().put("org.everrest.async.request", request);
    // Save current set of providers. In some environments they can be resource specific.
    job.getContext().put("org.everrest.async.providers", context.getProviders());

    initAsynchronousJobContext(job);

    final Long jobId = job.getJobId();
    jobs.put(jobId, job);

    try {
      pool.execute(job);
    } catch (RejectedExecutionException e) {
      jobs.remove(jobId);
      throw new AsynchronousJobRejectedException(e.getMessage());
    }

    LOG.debug("Add asynchronous job, ID: {}", jobId);

    return job;
  }

  private ContainerRequest createRequestCopy(
      GenericContainerRequest originRequest, SecurityContext securityContext) {
    // Create copy of request. Need to keep 'Accept' headers to be able determine MessageBodyWriter
    // which can be
    // used to serialize result of method invocation. Do not copy entity stream. This stream is
    // empty any way.
    return new ContainerRequest(
        originRequest.getMethod(),
        originRequest.getRequestUri(),
        originRequest.getBaseUri(),
        new EmptyInputStream(),
        originRequest.getRequestHeaders(),
        securityContext);
  }

  /**
   * Configures context of asynchronous job. This method is invoked by thread that adds new job.
   *
   * <p>This implementation does nothing, but may be customized in subclasses.
   *
   * @see AsynchronousJob#getContext()
   */
  protected void initAsynchronousJobContext(AsynchronousJob job) {}

  protected UriBuilder getAsynchronousJobUriBuilder(AsynchronousJob job) {
    return UriBuilder.fromPath(asynchronousServicePath).path(Long.toString(job.getJobId()));
  }

  protected Callable<Object> newCallable(Object resource, Method method, Object[] params) {
    return new MethodInvokeCallable(resource, method, params);
  }

  public AsynchronousJob getJob(Long jobId) {
    return jobs.get(jobId);
  }

  public AsynchronousJob removeJob(Long jobId) {
    final AsynchronousJob job = jobs.remove(jobId);
    if (!(job == null || job.isDone())) {
      job.cancel();
    }
    return job;
  }

  public List<AsynchronousJob> getAll() {
    return new ArrayList<>(jobs.values());
  }

  /**
   * Registers new listener if it is not registered yet.
   *
   * @param listener listener
   * @return {@code true} if new listener registered and {@code false} otherwise.
   * @see AsynchronousJobListener
   */
  public boolean registerListener(AsynchronousJobListener listener) {
    return jobListeners.addIfAbsent(listener);
  }

  /**
   * Unregisters listener.
   *
   * @param listener listener to unregister
   * @return {@code true} if listener unregistered and {@code false} otherwise.
   * @see AsynchronousJobListener
   */
  public boolean unregisterListener(AsynchronousJobListener listener) {
    return jobListeners.remove(listener);
  }

  @PreDestroy
  public void stop() {
    pool.shutdown();
    try {
      if (!pool.awaitTermination(5, SECONDS)) {
        pool.shutdownNow();
      }
    } catch (InterruptedException e) {
      pool.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  public static class ManyJobsPolicy implements RejectedExecutionHandler {
    private final RejectedExecutionHandler delegate;

    public ManyJobsPolicy(RejectedExecutionHandler delegate) {
      this.delegate = delegate;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
      if (executor.getPoolSize() >= executor.getCorePoolSize()) {
        throw new RejectedExecutionException(
            "Can't accept new asynchronous request. Too many asynchronous jobs in progress");
      }
      delegate.rejectedExecution(r, executor);
    }
  }
}
