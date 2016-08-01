package org.to2mbn.lolixl.ui.model;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.image.Image;

public interface DisplayableItem {

	ObservableStringValue getLocalizedName();

	default ObservableObjectValue<Image> getIcon() {
		return new SimpleObjectProperty<>(null);
	}

}
