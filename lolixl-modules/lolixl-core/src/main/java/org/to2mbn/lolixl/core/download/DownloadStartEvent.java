package org.to2mbn.lolixl.core.download;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import org.osgi.service.event.Event;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.DownloadCallbacks;
import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadTask;

public class DownloadStartEvent extends Event {

	public static final String TOPIC_DOWNLOAD_START = "org/to2mbn/lolixl/core/download/downloadStart";
	public static final String KEY_TASK = "org.to2mbn.lolixl.core.download.downloadStart.task";

	private static Map<String, Object> createProperties(DownloadTask<?> task) {
		Map<String, Object> properties = new HashMap<>();
		properties.put(KEY_TASK, task);
		return properties;
	}

	private DownloadTask<?> task;
	private List<DownloadCallback<Object>> callbacks = new Vector<>();

	public DownloadStartEvent(DownloadTask<?> task) {
		super(TOPIC_DOWNLOAD_START, createProperties(task));
		this.task = task;
	}

	public DownloadTask<?> getTask() {
		return task;
	}

	public void listen(DownloadCallback<Object> callback) {
		Objects.requireNonNull(callback);
		callbacks.add(callback);
	}

	@SuppressWarnings("unchecked")
	public <T> DownloadCallback<T> getRegisteredCallback() {
		return (DownloadCallback<T>) DownloadCallbacks.group(callbacks);
	}

}
