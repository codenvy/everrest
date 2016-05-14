/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.everrest.core.impl.async;

import org.everrest.core.impl.InternalException;
import org.everrest.core.resource.ResourceMethodDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

class AsynchronousFuture extends FutureTask<Object> implements AsynchronousJob {
    private static final Logger LOG = LoggerFactory.getLogger(AsynchronousFuture.class);

    private final List<AsynchronousJobListener> jobListeners;
    private final Long                     jobId;
    private final long                     expirationDate;
    private final ResourceMethodDescriptor method;
    private final Map<String, Object>      context;

    private String jobUri;

    AsynchronousFuture(Long jobId, Callable<Object> callable, long expirationDate, ResourceMethodDescriptor method,
                       List<AsynchronousJobListener> jobListeners) {
        super(callable);
        this.jobId = jobId;
        this.expirationDate = expirationDate;
        this.method = method;
        this.jobListeners = jobListeners;
        context = new HashMap<>();
    }

    @Override
    protected void done() {
        for (AsynchronousJobListener listener : jobListeners) {
            try {
                listener.done(this);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public Long getJobId() {
        return jobId;
    }

    @Override
    public String getJobURI() {
        return jobUri;
    }

    @Override
    public long getExpirationDate() {
        return expirationDate;
    }

    @Override
    public ResourceMethodDescriptor getResourceMethod() {
        return method;
    }

    @Override
    public boolean isDone() {
        return super.isDone();
    }

    @Override
    public boolean cancel() {
        return super.cancel(true);
    }

    @Override
    public Object getResult() throws IllegalStateException {
        if (!isDone()) {
            throw new IllegalStateException("Job is not done yet. ");
        }

        Object result;
        try {
            result = super.get();
        } catch (InterruptedException e) {
            // We already check the Future is done.
            throw new InternalException(e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof InvocationTargetException) {
                cause = ((InvocationTargetException)cause).getTargetException();
                if (cause instanceof WebApplicationException) {
                    throw (WebApplicationException)cause;
                }
            }
            throw new InternalException(cause);
        }

        return result;
    }

    @Override
    public Map<String, Object> getContext() {
        return context;
    }

    public void setJobURI(String jobURI) {
        this.jobUri = jobURI;
    }
}
