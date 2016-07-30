package org.to2mbn.lolixl.ui.impl.theme;

import org.to2mbn.lolixl.ui.theme.Theme;
import java.util.HashMap;
import java.util.Map;

public class DefaultTheme implements Theme {
	private final Map<String, Object> meta;

	public DefaultTheme() {
		meta = new HashMap<>();
		meta.put(Theme.PROPERTY_KEY_ID, "lolixl_default");
		meta.put(Theme.PROPERTY_KEY_AUTHORS, "LoliXL Developers");
		meta.put(Theme.PROPERTY_KEY_ICON_LOCATION, "/icon.png");
	}

	@Override
	public String getId() {
		return (String) meta.get(Theme.PROPERTY_KEY_ID);
	}

	@Override
	public Map<String, Object> getMeta() {
		return meta;
	}

	@Override
	public String[] getStyleSheets() {
		return new String[] { "/ui/css/default_theme/components.css", "/ui/css/default_theme/color_sets.css" };
	}

	@Override
	public ClassLoader getResourceLoader() {
		return getClass().getClassLoader();
	}
}
