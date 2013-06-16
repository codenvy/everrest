package org.everrest.core;

import junit.framework.TestCase;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** @author <a href="mailto:aparfonov@codenvy.com">Andrey Parfonov</a> */
public class HUTest {//extends TestCase {

    ThreadPoolExecutor       tp;
    ScheduledExecutorService stp;

    public void setUp() {
        tp = new CodenvyThreadPoolExecutor(1, 1,
                                           0L, TimeUnit.MILLISECONDS,
                                           new LinkedBlockingQueue<Runnable>());
        stp = Executors.newScheduledThreadPool(1);
    }

    public void ___test1() throws Exception {
        final int num = 10;
        final CountDownLatch latch = new CountDownLatch(num);
        for (int i = 0; i < num; i++) {
            new Thread() {
                public void run() {
                    ___do(latch);
                }
            }.start();
        }
        latch.await();
    }


    void _do(final CountDownLatch latch) {
        Foo.set(new Foo());
        tp.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Thread.sleep(300);
                p(Foo.get());
                latch.countDown();
                return null;
            }
        });
        Foo.set(null);
        p(Foo.get());
    }

    void __do(final CountDownLatch latch) {
        Foo.set(new Foo());
        tp.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(300);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                p(Foo.get());
                latch.countDown();
            }
        });
        Foo.set(null);
        p(Foo.get());
    }

    void ___do(final CountDownLatch latch) {
        Foo.set(new Foo());
        Callable c = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Thread.sleep(300);
                p(Foo.get());
                latch.countDown();
                return null;
            }
        };

        FutureTask<Object> f = new FutureTask<Object>(c) {
            @Override
            protected void done() {
                p("done");
            }
        };

        tp.execute(f);
        Foo.set(null);
        p(Foo.get());
    }

    void ____do(final CountDownLatch latch) {
        Foo.set(new Foo());
        stp.scheduleAtFixedRate(CodenvyThreadPoolExecutor.wrap(new Runnable() {
            int i = 0;

            @Override
            public void run() {
                try {
                    Thread.sleep(300);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                p(++i);
                p(Foo.get());
                if (i > 3) {
                    latch.countDown();
                }
            }
        }), 500, 300, TimeUnit.MILLISECONDS);
        Foo.set(null);
        p(Foo.get());
    }

    static void p(Object o) {
        System.out.printf("\t%s: %s%n", Thread.currentThread().getName(), o);
    }

    static class CodenvyThreadPoolExecutor extends ThreadPoolExecutor {

        public CodenvyThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                         BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        public CodenvyThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                         BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        }

        public CodenvyThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                         BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
        }

        public CodenvyThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                         BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
                                         RejectedExecutionHandler handler) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        }

//        @Override
//        protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
//            return super.newTaskFor(new RunnableUsingCurrentEnvironment(runnable), value);
//        }
//
//        @Override
//        protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
//            return super.newTaskFor(new CallableUsingCurrentEnvironment<T>(callable));
//        }

        public static Runnable wrap(Runnable runnable) {
            return new RunnableUsingCurrentEnvironment(runnable);
        }

        public static <T> Callable<T> wrap(Callable<T> callable) {
            return new CallableUsingCurrentEnvironment<T>(callable);
        }

        @Override
        public void execute(Runnable command) {
            if (command == null) {
                super.execute(command); // Let's ThreadPoolExecutor throws NullPointerException.
            }
            super.execute(wrap(command));
        }
    }


    static class Foo {
        static ThreadLocal<Foo> tl = new ThreadLocal<Foo>();

        static Foo get() {
            return tl.get();
        }

        static void set(Foo f) {
            tl.set(f);
        }

        public String toString() {
            return "I'm foo!";
        }
    }

    static class CallableUsingCurrentEnvironment<V> implements Callable<V> {
        private final Callable<V> c;
        private final Foo         foo;

        CallableUsingCurrentEnvironment(Callable<V> c) {
            this.c = c;
            foo = Foo.tl.get();
        }

        @Override
        public V call() throws Exception {
            try {
                Foo.set(foo);
                return c.call();
            } finally {
                Foo.set(null);
            }
        }
    }

    static class RunnableUsingCurrentEnvironment implements Runnable {
        private final Runnable r;
        private final Foo      foo;

        RunnableUsingCurrentEnvironment(Runnable r) {
            this.r = r;
            foo = Foo.tl.get();
        }

        @Override
        public void run() {
            try {
                Foo.set(foo);
                r.run();
            } finally {
                Foo.set(null);
            }
        }
    }
}
