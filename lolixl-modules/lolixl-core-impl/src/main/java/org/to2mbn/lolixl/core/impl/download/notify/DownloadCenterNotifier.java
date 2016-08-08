package org.to2mbn.lolixl.core.impl.download.notify;

import java.util.function.Consumer;

public interface DownloadCenterNotifier {

	public void forEachChangedTask(Consumer<DownloadTaskGroup> action, boolean getAll);

}
