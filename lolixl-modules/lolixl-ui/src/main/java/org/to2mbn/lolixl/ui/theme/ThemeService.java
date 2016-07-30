package org.to2mbn.lolixl.ui.theme;

import java.util.List;

public interface ThemeService {

	void enable(Theme theme);
	void disable(Theme theme);

	List<Theme> getEnabledThemes();
	List<Theme> getDisabledThemes();

}
