package org.to2mbn.lolixl.core.game.download;

import org.to2mbn.lolixl.ui.DisplayableItem;
import javafx.collections.ObservableList;

public interface VersionsGroup<VER extends DownloadableVersion> extends DisplayableItem {

	ObservableList<VER> getVersions();

}
