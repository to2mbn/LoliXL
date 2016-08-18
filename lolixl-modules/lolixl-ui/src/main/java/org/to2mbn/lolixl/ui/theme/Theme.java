package org.to2mbn.lolixl.ui.theme;

import org.to2mbn.lolixl.ui.model.DisplayableItem;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;

public interface Theme extends DisplayableItem {

	String PROPERTY_THEME_ID = "org.to2mbn.lolixl.ui.theme.id";
	String PROPERTY_THEME_TYPE = "org.to2mbn.lolixl.ui.theme.type";

	String TYPE_THEME_PACKAGE = "theme-package";
	String TYPE_FONT = "font";
	String TYPE_COLOR = "color";

	String[] getStyleSheets();

	default String getURI() {
		return null;
	}

	default ObservableValue<ObservableStringValue[]> getAuthors() {
		return null;
	}

	default ObservableStringValue getDescription() {
		return null;
	}

}
