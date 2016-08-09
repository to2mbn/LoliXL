package org.to2mbn.lolixl.core.impl.download.notify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.to2mbn.jmccc.mcdownloader.CacheOption;
import org.to2mbn.jmccc.mcdownloader.MinecraftDownloadOption;
import org.to2mbn.jmccc.mcdownloader.MinecraftDownloader;
import org.to2mbn.jmccc.mcdownloader.RemoteVersionList;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloadTask;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.CombinedDownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.CombinedDownloadCallbacks;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.DownloadCallback;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.DownloadCallbacks;
import org.to2mbn.jmccc.mcdownloader.download.tasks.DownloadTask;
import org.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.Version;
import org.to2mbn.lolixl.core.download.CompletableFutureAdapter;
import org.to2mbn.lolixl.core.download.DownloadCenter;
import org.to2mbn.lolixl.core.download.DownloadStartEvent;
import org.to2mbn.lolixl.ui.model.DisplayableItem;

@Service({ DownloadCenter.class, DownloadCenterNotifier.class, EventHandler.class })
@Properties({
		@Property(name = EventConstants.EVENT_TOPIC, value = DownloadStartEvent.TOPIC_DOWNLOAD_START)
})
@Component(immediate = true)
public class DownloadCenterImpl implements DownloadCenter, EventHandler, DownloadCenterNotifier {

	static void updateRetryInfo(DownloadTaskEntry entry, Throwable e, int current, int max) {
		RetryInfo retryInfo = new RetryInfo();
		retryInfo.current = current;
		retryInfo.max = max;
		retryInfo.ex = e;
		entry.retryInfo = retryInfo;
		changedEntry(entry);
	}

	static void changedEntry(DownloadTaskEntry entry) {
		if (entry.online)
			entry.parent.pendingSubtasks.add(entry);
	}

	void changedGroup(DownloadTaskGroup group) {
		pendingGroups.add(group);
	}

	void groupDone(DownloadTaskGroup group) {
		allGroups.remove(group);
		changedGroup(group);
	}

	class ProxiedMinecraftDownloader implements MinecraftDownloader {

		DownloadTaskGroup result;
		boolean available;
		Thread thread;
		Future<?> submittedFuture;

		final void checkState() {
			if (!available)
				throw new IllegalStateException("Out of scope");
			if (Thread.currentThread() != thread)
				throw new IllegalStateException("Except thread: " + thread.getName());
			if (result != null)
				throw new IllegalStateException("Only one task can be submitted");
		}

		final void entryDone(DownloadTaskEntry entry) {
			allOfflineTasks.remove(entry.task);
			if (entry.online) {
				result.finishedCount.getAndIncrement();
				entry.parent.pendingSubtasks.add(entry);
				changedGroup(result);
			}

		}

		final <T> Future<T> handleCombinedDownloadTask(CombinedDownloadCallback<T> callback, Function<CombinedDownloadCallback<T>, Future<T>> action) {
			checkState();
			result = new DownloadTaskGroup();
			CompletableFutureAdapter<T> adapter = new CompletableFutureAdapter<>();
			result.future = adapter.toCompletableFuture();
			List<CombinedDownloadCallback<T>> callbacks = new ArrayList<>();
			callbacks.add(new CombinedDownloadCallback<T>() {

				@Override
				public void done(T dummy) {
					groupDone(result);
				}

				@Override
				public void failed(Throwable e) {
					result.exception = e;
					groupDone(result);
				}

				@Override
				public void cancelled() {
					groupDone(result);
				}

				@Override
				public <R> DownloadCallback<R> taskStart(DownloadTask<R> task) {
					DownloadTaskEntry entry = new DownloadTaskEntry();
					entry.parent = result;
					entry.task = task;
					CompletableFutureAdapter<R> subadapter = new CompletableFutureAdapter<>();
					entry.future = subadapter.toCompletableFuture();
					List<DownloadCallback<R>> subcallbacks = new ArrayList<>();
					subcallbacks.add(new DownloadCallback<R>() {

						@Override
						public void done(R dummy) {
							entryDone(entry);
						}

						@Override
						public void failed(Throwable e) {
							entry.exception = e;
							entryDone(entry);
						}

						@Override
						public void cancelled() {
							entryDone(entry);
						}

						@Override
						public void updateProgress(long done, long total) {
							Progress subtaskProgress = new Progress();
							subtaskProgress.done = done;
							subtaskProgress.total = total;
							Progress lastSubtaskProgress = entry.progress.getAndSet(subtaskProgress);

							Progress newTotalProgress = new Progress();
							Progress expectTotalProgress;

							do {
								expectTotalProgress = result.progress.get();
								// Δ = current - last
								// total' = total + Δ
								//        = total + current - last
								newTotalProgress.done = expectTotalProgress.done + done - lastSubtaskProgress.done;
								newTotalProgress.total = expectTotalProgress.total + total - lastSubtaskProgress.total;
							} while (result.progress.compareAndSet(expectTotalProgress, newTotalProgress));
							changedEntry(entry);
							changedGroup(result);
						}

						@Override
						public void retry(Throwable e, int current, int max) {
							updateRetryInfo(entry, e, current, max);
						}
					});
					subcallbacks.add(DownloadCallbacks.fromCallback(subadapter));
					allOfflineTasks.put(task, entry);
					return DownloadCallbacks.group(subcallbacks);
				}
			});
			callbacks.add(CombinedDownloadCallbacks.fromCallback(adapter));
			if (callback != null)
				callbacks.add(callback);
			Future<T> cancelableFuture = action.apply(CombinedDownloadCallbacks.group(callbacks));
			result.cancelCallback = mayInterruptIfRunning -> cancelableFuture.cancel(mayInterruptIfRunning);
			adapter.setAdaptedCancelable(dummy -> cancelableFuture.cancel(true));
			submittedFuture = cancelableFuture;
			return cancelableFuture;
		}

		final <T> Future<T> handleDownloadTask(DownloadTask<T> task, DownloadCallback<T> callback, Function<DownloadCallback<T>, Future<T>> action) {
			checkState();
			result = new DownloadTaskGroup();
			CompletableFutureAdapter<T> adapter = new CompletableFutureAdapter<>();
			result.future = adapter.toCompletableFuture();
			DownloadTaskEntry entry = new DownloadTaskEntry();
			entry.parent = result;
			entry.future = result.future;
			entry.task = task;
			allOfflineTasks.put(task, entry);

			List<DownloadCallback<T>> callbacks = new ArrayList<>();
			callbacks.add(new DownloadCallback<T>() {

				@Override
				public void done(T dummy) {
					entryDone(entry);
					groupDone(result);
				}

				@Override
				public void failed(Throwable e) {
					result.exception = e;
					entry.exception = e;
					entryDone(entry);
					groupDone(result);
				}

				@Override
				public void cancelled() {
					entryDone(entry);
					groupDone(result);
				}

				@Override
				public void updateProgress(long done, long total) {
					Progress progress = new Progress();
					progress.done = done;
					progress.total = total;
					entry.progress.set(progress);
					result.progress.set(progress);
					changedEntry(entry);
					changedGroup(result);
				}

				@Override
				public void retry(Throwable e, int current, int max) {
					updateRetryInfo(entry, e, current, max);
				}
			});
			callbacks.add(DownloadCallbacks.fromCallback(adapter));
			if (callback != null)
				callbacks.add(callback);
			Future<T> cancelableFuture = action.apply(DownloadCallbacks.group(callbacks));
			result.cancelCallback = mayInterruptIfRunning -> cancelableFuture.cancel(mayInterruptIfRunning);
			adapter.setAdaptedCancelable(dummy -> cancelableFuture.cancel(true));
			submittedFuture = cancelableFuture;
			return cancelableFuture;
		}

		@Override
		public <T> Future<T> download(CombinedDownloadTask<T> task, CombinedDownloadCallback<T> callback) {
			return handleCombinedDownloadTask(callback, newCallback -> underlying.download(task, newCallback));
		}

		@Override
		public <T> Future<T> download(CombinedDownloadTask<T> task, CombinedDownloadCallback<T> callback, int tries) {
			return handleCombinedDownloadTask(callback, newCallback -> underlying.download(task, newCallback, tries));
		}

		@Override
		public <T> Future<T> download(DownloadTask<T> task, DownloadCallback<T> callback) {
			return handleDownloadTask(task, callback, newCallback -> underlying.download(task, newCallback));
		}

		@Override
		public <T> Future<T> download(DownloadTask<T> task, DownloadCallback<T> callback, int tries) {
			return handleDownloadTask(task, callback, newCallback -> underlying.download(task, newCallback, tries));
		}

		@Override
		public Future<Version> downloadIncrementally(MinecraftDirectory dir, String version, CombinedDownloadCallback<Version> callback, MinecraftDownloadOption... options) {
			return handleCombinedDownloadTask(callback, newCallback -> underlying.downloadIncrementally(dir, version, newCallback, options));
		}

		@Override
		public Future<RemoteVersionList> fetchRemoteVersionList(CombinedDownloadCallback<RemoteVersionList> callback, CacheOption... options) {
			return handleCombinedDownloadTask(callback, newCallback -> underlying.fetchRemoteVersionList(newCallback, options));
		}

		@Override
		public MinecraftDownloadProvider getProvider() {
			return underlying.getProvider();
		}

		@Override
		public void shutdown() {}

		@Override
		public boolean isShutdown() {
			return underlying.isShutdown();
		}

	}

	@Reference
	MinecraftDownloader underlying;

	Map<DownloadTask<?>, DownloadTaskEntry> allOfflineTasks = new ConcurrentHashMap<>();
	Set<DownloadTaskGroup> allGroups = new ConcurrentSkipListSet<>();
	Set<DownloadTaskGroup> pendingGroups = new ConcurrentSkipListSet<>();

	@Override
	public <T> CompletableFuture<T> startTask(DisplayableItem itemToDisplay, Function<MinecraftDownloader, Future<T>> operation) {
		Objects.requireNonNull(itemToDisplay);
		Objects.requireNonNull(operation);

		ProxiedMinecraftDownloader proxied = new ProxiedMinecraftDownloader();
		proxied.thread = Thread.currentThread();
		proxied.available = true;

		DownloadTaskGroup result;
		Future<T> returnedFuture;
		Future<?> submittedFuture;
		try {
			returnedFuture = operation.apply(proxied);
		} finally {
			result = proxied.result;
			submittedFuture = proxied.submittedFuture;
			proxied.available = false;
			proxied.thread = null;
		}

		if (result == null) {
			throw new IllegalStateException("No task has been submitted");
		}

		if (submittedFuture == null) {
			throw new IllegalStateException("operation returned null");
		}

		if (submittedFuture != returnedFuture) {
			throw new IllegalStateException("submittedFuture != returnedFuture");
		}

		result.displayableItem = itemToDisplay;

		allGroups.add(result);
		changedGroup(result);

		@SuppressWarnings("unchecked")
		CompletableFuture<T> completableFuture = (CompletableFuture<T>) result.future;

		return completableFuture;
	}

	@Override
	public void handleEvent(Event event) {
		if (event instanceof DownloadStartEvent) {
			DownloadTaskEntry entry = allOfflineTasks.remove(((DownloadStartEvent) event).getTask());
			if (entry != null) {
				entry.online = true;
				entry.idx = entry.parent.totalCount.getAndIncrement();
				entry.parent.onlineTasks.add(entry);
				changedEntry(entry);
				changedGroup(entry.parent);
			}
		}
	}

	@Override
	public void forEachChangedTask(Consumer<DownloadTaskGroup> action, boolean getAll) {
		if (getAll) {
			pendingGroups.clear();
			allGroups.forEach(action);
		} else {
			for (DownloadTaskGroup group : pendingGroups) {
				pendingGroups.remove(group);
				action.accept(group);
			}
		}
	}

}
