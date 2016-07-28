package org.to2mbn.lolixl.core.download;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;
import org.to2mbn.jmccc.mcdownloader.MinecraftDownloader;
import org.to2mbn.lolixl.ui.model.DisplayableItem;

public interface DownloadCenter {

	<T> CompletableFuture<T> startTask(DisplayableItem itemToDisplay, Function<MinecraftDownloader, Future<T>> operation);

}
