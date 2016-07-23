package org.to2mbn.lolixl.core.download.notify;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.to2mbn.lolixl.core.ui.DisplayableItem;

public class DownloadTaskGroup extends AbstractDownloadTaskModel {

	DisplayableItem displayableItem;
	AtomicInteger totalCount = new AtomicInteger();
	AtomicInteger finishedCount = new AtomicInteger();
	Set<DownloadTaskEntry> onlineTasks = new ConcurrentSkipListSet<>();
	Set<DownloadTaskEntry> pendingSubtasks = new ConcurrentSkipListSet<>();

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

}
