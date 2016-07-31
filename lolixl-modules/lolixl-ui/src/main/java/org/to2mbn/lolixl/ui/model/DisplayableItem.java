package org.to2mbn.lolixl.ui.model;

import javafx.beans.value.ObservableStringValue;
import javafx.scene.image.Image;

public interface DisplayableItem {

	ObservableStringValue getLocalizedName();

	default Image getIcon() {
		return null;
	}

}
