package org.to2mbn.lolixl.core.game.download;

import java.util.List;
import org.to2mbn.lolixl.ui.component.DisplayableItem;

public interface VersionsGroup<VER extends DownloadableVersion> extends DisplayableItem {

	List<VER> getVersions();

}
