package org.to2mbn.lolixl.ui.model;

import com.sun.javafx.binding.ObjectConstant;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.image.Image;

public interface DisplayableItem {

	ObservableStringValue getLocalizedName();

	default ObservableObjectValue<Image> getIcon() {
		return ObjectConstant.valueOf(null);
	}

}
