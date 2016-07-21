package org.to2mbn.lolixl.core.game.download;

import java.util.Set;
import org.to2mbn.lolixl.core.ui.DisplayableTile;
import javafx.scene.layout.Region;

public interface DownloadableVersion extends DisplayableTile {

	Set<String> getSearchTags();

	Region createDescriptionComponent();

	void download();

}
