package org.to2mbn.lolixl.ui.theme;

import java.util.Map;

public interface Theme {
	String PROPERTY_FILE_NAME = "meta.json";
	String PROPERTY_KEY_ID = "id";
	String PROPERTY_KEY_AUTHORS = "authors";
	String PROPERTY_KEY_ICON_LOCATION = "icon";
	String PROPERTY_KEY_DESCRIPTION = "description";
	String INTERNAL_PROPERTY_KEY_PACKAGE_PATH = "package_path";

	String getId();

	Map<String, Object> getMeta();

	String[] getStyleSheets();

	ClassLoader getResourceLoader();
}
