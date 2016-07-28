package org.to2mbn.lolixl.ui.theme;

import java.util.Map;

public interface Theme {
	String META_KEY_ID = "id";
	String META_KEY_AUTHROS = "authors";

	Map<String, Object> getMeta();

	String[] getStyleSheets();
}
