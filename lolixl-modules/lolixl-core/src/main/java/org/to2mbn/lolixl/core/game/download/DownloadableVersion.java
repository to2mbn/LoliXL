package org.to2mbn.lolixl.core.game.download;

import java.util.Set;
import org.to2mbn.lolixl.ui.DisplayableTile;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Region;

public interface DownloadableVersion extends DisplayableTile {

	ObservableValue<Set<String>> getSearchTags();

	Region createDescriptionComponent();

	void download();

}
