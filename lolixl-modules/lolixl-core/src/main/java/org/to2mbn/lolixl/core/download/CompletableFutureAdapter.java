package org.to2mbn.lolixl.core.download;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.Callback;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.Cancelable;

/**
 * Adapts JMCCC Callback -> Java CompletableFuture
 * 
 * @author yushijinhun
 * @param <T> result type
 */
public class CompletableFutureAdapter<T> implements Callback<T> {

	private CompletableFuture<T> adapted = new CompletableFuture<T>() {

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			if (isDone()) {
				return true;
			} else {
				Cancelable cancelable = adaptedCancelable;
				if (cancelable != null && underlyingCancelled.compareAndSet(false, true)) {
					cancelable.cancel(true);
				}
				super.cancel(true);
				return cancelable != null || isDone();
			}
		}

	};

	private volatile Cancelable adaptedCancelable;
	private AtomicBoolean underlyingCancelled = new AtomicBoolean();

	public void setAdaptedCancelable(Future<?> adaptedCancelableFuture) {
		setAdaptedCancelable(mayInterruptIfRunning -> adaptedCancelableFuture.cancel(mayInterruptIfRunning));
	}

	public void setAdaptedCancelable(Cancelable adaptedCancelable) {
		this.adaptedCancelable = adaptedCancelable;
		if (adapted.isCancelled() && underlyingCancelled.compareAndSet(false, true)) {
			adaptedCancelable.cancel(true);
		}
	}

	public CompletableFuture<T> toCompletableFuture() {
		return adapted;
	}

	@Override
	public void done(T result) {
		adapted.complete(result);
	}

	@Override
	public void failed(Throwable e) {
		adapted.completeExceptionally(e);
	}

	@Override
	public void cancelled() {
		if (underlyingCancelled.compareAndSet(false, true)) {
			adapted.cancel(true);
		}
	}

}
