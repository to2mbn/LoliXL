package org.to2mbn.lolixl.core.internal;

import java.util.Objects;
import java.util.concurrent.Future;
import org.osgi.service.event.EventAdmin;
import org.to2mbn.jmccc.mcdownloader.download.Downloader;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.DownloadCallbacks;
import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadTask;
import org.to2mbn.lolixl.core.event.DownloadStartEvent;

public class DownloaderWrapper implements Downloader {

	private Downloader underlying;
	private EventAdmin eventAdmin;

	public DownloaderWrapper(Downloader underlying, EventAdmin eventAdmin) {
		this.underlying = Objects.requireNonNull(underlying);
		this.eventAdmin = Objects.requireNonNull(eventAdmin);
	}

	@Override
	public void shutdown() {
		underlying.shutdown();
	}

	@Override
	public boolean isShutdown() {
		return underlying.isShutdown();
	}

	@Override
	public <T> Future<T> download(DownloadTask<T> task, DownloadCallback<T> callback) {
		return underlying.download(task, postEvent(task, callback));
	}

	@Override
	public <T> Future<T> download(DownloadTask<T> task, DownloadCallback<T> callback, int tries) {
		return underlying.download(task, postEvent(task, callback), tries);
	}

	@Override
	public String toString() {
		return String.format("DownloaderWrapper [underlying=%s]", underlying);
	}

	private <T> DownloadCallback<T> postEvent(DownloadTask<T> task, DownloadCallback<T> originCallback) {
		DownloadStartEvent event = new DownloadStartEvent(task);
		eventAdmin.sendEvent(event);
		if (originCallback == null) {
			return event.getRegisteredCallback();
		} else {
			return DownloadCallbacks.group(originCallback, event.getRegisteredCallback());
		}
	}

}
