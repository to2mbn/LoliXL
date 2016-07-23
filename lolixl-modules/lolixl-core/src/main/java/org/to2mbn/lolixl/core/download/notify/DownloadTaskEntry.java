package org.to2mbn.lolixl.core.download.notify;

import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadTask;

public class DownloadTaskEntry extends AbstractDownloadTaskModel {

	DownloadTaskGroup parent;
	DownloadTask<?> task;
	volatile RetryInfo retryInfo;
	volatile boolean online;

	public DownloadTask<?> getTask() {
		return task;
	}

	/**
	 * @return 上一次重试时的信息，可能为null
	 */
	public RetryInfo getLastRetry() {
		return retryInfo;
	}
}