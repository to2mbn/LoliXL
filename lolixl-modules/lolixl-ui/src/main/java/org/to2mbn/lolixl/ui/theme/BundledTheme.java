package org.to2mbn.lolixl.ui.theme;

public interface BundledTheme extends Theme {
	String META_KEY_ICON_LOCATION = "icon";
	String INTERNAL_META_KEY_BUNDLE_URL = "bundle_url";

	ClassLoader getResourceLoader();
}
