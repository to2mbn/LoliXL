package org.to2mbn.lolixl.ui.theme.management;

import org.to2mbn.lolixl.ui.theme.Theme;
import org.to2mbn.lolixl.ui.theme.exception.InvalidThemeException;

public interface ThemeManagementService {
	void installTheme(Theme theme) throws InvalidThemeException;

	void uninstallTheme(Theme theme);

	Theme getInstalledTheme();
}
