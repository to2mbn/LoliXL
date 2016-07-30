package org.to2mbn.lolixl.ui.theme;

import org.to2mbn.lolixl.ui.model.DisplayableItem;

public interface Theme extends DisplayableItem {

	String PROPERTY_THEME_ID = "org.to2mbn.lolixl.ui.theme.id";

	String[] getStyleSheets();

	default ClassLoader getResourceLoader() {
		return getClass().getClassLoader();
	}

	default String getURI() {
		return null;
	}

	default String[] getAuthors() {
		return null;
	}

	default String getDescription() {
		return null;
	}

}
