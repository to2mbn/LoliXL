package org.to2mbn.lolixl.core.game.download;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.to2mbn.lolixl.ui.model.DisplayableItem;

public interface VersionTypeProvider<VER extends DownloadableVersion> extends DisplayableItem {

	String PROPERTY_VERSION_TYPE = "org.to2mbn.lolixl.core.game.download.type";

	CompletableFuture<List<VersionsGroup<VER>>> getVersionList();

}
