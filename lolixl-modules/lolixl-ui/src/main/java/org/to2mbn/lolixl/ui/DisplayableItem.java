package org.to2mbn.lolixl.ui;

import org.to2mbn.lolixl.utils.binding.FxConstants;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.image.Image;

public interface DisplayableItem {

	ObservableStringValue getLocalizedName();

	default ObservableObjectValue<Image> getIcon() {
		return FxConstants.object(null);
	}

}
