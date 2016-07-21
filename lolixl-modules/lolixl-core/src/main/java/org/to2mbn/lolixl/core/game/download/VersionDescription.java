package org.to2mbn.lolixl.core.game.download;

import org.to2mbn.lolixl.core.ui.DisplayableItem;
import javafx.scene.layout.Region;

public interface VersionDescription extends DisplayableItem {

	Region createDescriptionComponent();

	void download();

}
