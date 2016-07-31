package org.to2mbn.lolixl.core.game.download;

import org.to2mbn.lolixl.ui.model.DisplayableItem;
import javafx.collections.ObservableList;

public interface VersionTypeProvider<VER extends DownloadableVersion> extends DisplayableItem {

	String PROPERTY_VERSION_TYPE = "org.to2mbn.lolixl.core.game.download.type";

	ObservableList<VersionsGroup<VER>> getVersionList();

}
