package org.to2mbn.lolixl.core.download.notify;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

abstract public class AbstractDownloadTaskModel implements Comparable<AbstractDownloadTaskModel> {

	int idx;
	CompletableFuture<?> future;
	AtomicReference<Progress> progress = new AtomicReference<>(new Progress());
	volatile Throwable exception;

	/**
	 * @return 该节点的索引，仅用于按时间排序（越晚索引越大）
	 */
	public int getIndex() {
		return idx;
	}

	public Progress getProgress() {
		return progress.get();
	}

	/**
	 * @return 如果该任务因异常中止，则返回此异常，否则返回null
	 */
	public Throwable getException() {
		return exception;
	}

	/**
	 * @return 任务是否因取消而中止
	 */
	public boolean isCancelled() {
		return future.isCancelled();
	}

	/**
	 * @return 任务是否已完成（成功、失败或取消）
	 */
	public boolean isDone() {
		return future.isDone();
	}

	@Override
	public int compareTo(AbstractDownloadTaskModel o) {
		return idx - o.idx;
	}

}
