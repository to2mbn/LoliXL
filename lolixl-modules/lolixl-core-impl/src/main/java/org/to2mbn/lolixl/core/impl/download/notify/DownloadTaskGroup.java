package org.to2mbn.lolixl.core.impl.download.notify;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.Cancelable;
import org.to2mbn.lolixl.ui.DisplayableItem;

public class DownloadTaskGroup extends AbstractDownloadTaskModel implements Cancelable {

	DisplayableItem displayableItem;
	AtomicInteger totalCount = new AtomicInteger();
	AtomicInteger finishedCount = new AtomicInteger();
	Set<DownloadTaskEntry> onlineTasks = new ConcurrentSkipListSet<>();
	Set<DownloadTaskEntry> pendingSubtasks = new ConcurrentSkipListSet<>();
	volatile Cancelable cancelCallback;

	public void forEachChangedEntry(Consumer<DownloadTaskEntry> action, boolean getAll) {
		if (getAll) {
			pendingSubtasks.clear();
			onlineTasks.forEach(action);
		} else {
			for (DownloadTaskEntry entry : pendingSubtasks) {
				pendingSubtasks.remove(entry);
				action.accept(entry);
			}
		}
	}

	public DisplayableItem getDisplayableItem() {
		return displayableItem;
	}

	/**
	 * @return 总任务数
	 */
	public int getTotalCount() {
		return totalCount.get();
	}

	/**
	 * @return 完成任务数
	 */
	public int getFinishedCount() {
		return finishedCount.get();
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		if (cancelCallback == null) {
			return false;
		}
		return cancelCallback.cancel(mayInterruptIfRunning);
	}

}
