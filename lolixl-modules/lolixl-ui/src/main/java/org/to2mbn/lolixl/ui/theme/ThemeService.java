package org.to2mbn.lolixl.ui.theme;

import javafx.collections.ObservableList;

public interface ThemeService {

	void enable(Theme theme, boolean forcibly);
	void disable(Theme theme, boolean forcibly);

	ObservableList<Theme> getEnabledThemes();
	ObservableList<Theme> getDisabledThemes();
	ObservableList<Theme> getAllThemes();

}
