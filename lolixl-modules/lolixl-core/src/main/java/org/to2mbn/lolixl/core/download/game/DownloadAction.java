package org.to2mbn.lolixl.core.download.game;

import java.util.concurrent.CompletableFuture;

public interface DownloadAction {

	String getLocalizedName();

	CompletableFuture<String> getVersionToDownload();

}
