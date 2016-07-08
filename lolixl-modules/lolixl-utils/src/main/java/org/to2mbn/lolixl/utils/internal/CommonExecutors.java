package org.to2mbn.lolixl.utils.internal;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.apache.felix.scr.annotations.Component;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

@Component
public class CommonExecutors implements BundleActivator {

	@Override
	public void start(BundleContext ctx) {
		addExecutor(ctx, "local_io", pool(Runtime.getRuntime().availableProcessors() * 2, 10, TimeUnit.SECONDS, "lolixl.local_io"));
	}

	private static class NamedThreadFactory implements ThreadFactory {

		private final String name;
		private final AtomicInteger threadNumber = new AtomicInteger(1);

		public NamedThreadFactory(String name) {
			this.name = name;
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, "pool-" + name + "-thread-" + threadNumber.getAndIncrement());
			return t;
		}

	}

	public static Supplier<ExecutorService> pool(int threads, long keepAliveTime, TimeUnit unit, String poolName) {
		return () -> {
			ThreadPoolExecutor pool = new ThreadPoolExecutor(threads, threads, keepAliveTime, unit, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory(poolName));
			pool.allowCoreThreadTimeOut(true);
			return pool;
		};
	}

	private Map<String, ExecutorService> executors = new HashMap<>();

	private void addExecutor(BundleContext ctx, String usage, Supplier<ExecutorService> executorFactory) {
		synchronized (executors) {
			if (executors.containsKey(usage)) {
				throw new IllegalArgumentException("Executor [" + usage + "] has been registered");
			}
			ExecutorService executor = executorFactory.get();
			executors.put(usage, executor);
			Dictionary<String, String> properties = new Hashtable<>();
			properties.put("usage", usage);
			ctx.registerService(ExecutorService.class, executor, properties);
		}
	}

	@Override
	public void stop(BundleContext ctx) {
		synchronized (executors) {
			Throwable exception = null;
			for (ExecutorService executor : executors.values()) {
				try {
					executor.shutdownNow();
				} catch (Throwable e) {
					if (exception == null) {
						exception = e;
					} else {
						exception.addSuppressed(e);
					}
				}
			}
		}
	}

}
