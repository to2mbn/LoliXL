package org.to2mbn.lolixl.ui.theme;

import javafx.collections.ObservableList;

public interface ThemeService {

	void enable(Theme theme);
	void disable(Theme theme);

	ObservableList<Theme> getEnabledThemes();
	ObservableList<Theme> getDisabledThemes();
	ObservableList<Theme> getAllThemes();

}
