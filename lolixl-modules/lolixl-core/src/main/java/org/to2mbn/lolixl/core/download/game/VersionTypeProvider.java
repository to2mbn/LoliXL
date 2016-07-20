package org.to2mbn.lolixl.core.download.game;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import javafx.scene.image.Image;

public interface VersionTypeProvider<VER extends DownloadableVersion> {

	String PROPERTY_VERSION_TYPE = "org.to2mbn.lolixl.core.download.game.type";

	// TODO: 也许抽取这两个方法为超接口？
	String getLocalizedName();
	Image getIcon();

	CompletableFuture<List<VersionsGroup<VER>>> getVersionList();

	VersionDescription getDescription(VER version);

}
