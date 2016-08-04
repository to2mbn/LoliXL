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
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

@Component
public class CommonExecutors {

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

	private static Map<String, ExecutorService> executors = new HashMap<>();

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

	@Activate
	public void active(ComponentContext compCtx) {
		BundleContext ctx = compCtx.getBundleContext();
		addExecutor(ctx, "local_io", pool(Runtime.getRuntime().availableProcessors() * 2, 10, TimeUnit.SECONDS, "lolixl.local_io"));
		addExecutor(ctx, "cpu_compute", pool(Runtime.getRuntime().availableProcessors(), 30, TimeUnit.SECONDS, "lolixl.cpu_compute"));
	}

	@Deactivate
	public void deactivate() {
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

	public static ExecutorService getExecutorService(String usage) {
		return executors.get(usage);
	}

}
