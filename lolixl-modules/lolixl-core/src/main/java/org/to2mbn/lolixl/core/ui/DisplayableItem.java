package org.to2mbn.lolixl.core.ui;

import javafx.scene.image.Image;

public interface DisplayableItem {

	String getLocalizedName();

	default Image getIcon() {
		return null;
	}

}
