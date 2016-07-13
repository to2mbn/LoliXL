package org.to2mbn.lolixl.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

public final class AsyncUtils {

	private AsyncUtils() {}

	public static boolean exceptionInstanceof(Class<? extends Throwable> exClass, Throwable ex) {
		return exClass.isInstance(ex) ||
				(ex instanceof CompletionException && exClass.isInstance(ex.getCause()));
	}

	public static <T> CompletableFuture<T> asyncRun(Callable<T> callable, Executor executor) {
		CompletableFuture<T> future = new CompletableFuture<>();
		executor.execute(() -> {
			try {
				future.complete(callable.call());
			} catch (Throwable e) {
				future.completeExceptionally(e);
			}
		});
		return future;
	}

}
