package org.to2mbn.lolixl.ui.theme.management;

import org.to2mbn.lolixl.ui.theme.Theme;
import org.to2mbn.lolixl.ui.theme.exception.InvalidBundledThemeException;

public interface ThemeManagementService {
	void installTheme(Theme theme) throws InvalidBundledThemeException;

	void uninstallTheme(Theme theme);

	Theme getInstalledTheme();
}
