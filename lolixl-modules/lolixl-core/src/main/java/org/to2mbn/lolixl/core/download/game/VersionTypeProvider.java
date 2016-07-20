package org.to2mbn.lolixl.core.download.game;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface VersionTypeProvider<VER extends DownloadableVersion> {

	String PROPERTY_VERSION_TYPE = "org.to2mbn.lolixl.core.download.game.type";

	String getLocalizedName();

	CompletableFuture<List<VersionsGroup<VER>>> getVersionList();

	VersionDescription getDescription(VER version);

}
