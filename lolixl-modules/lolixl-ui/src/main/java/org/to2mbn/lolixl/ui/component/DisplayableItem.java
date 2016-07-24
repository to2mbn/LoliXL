package org.to2mbn.lolixl.ui.component;

import javafx.scene.image.Image;

public interface DisplayableItem {

	String getLocalizedName();

	default Image getIcon() {
		return null;
	}

}
