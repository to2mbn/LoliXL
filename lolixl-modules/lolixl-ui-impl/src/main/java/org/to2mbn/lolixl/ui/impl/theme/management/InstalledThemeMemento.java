package org.to2mbn.lolixl.ui.impl.theme.management;

import org.to2mbn.lolixl.core.config.Configuration;

import java.util.ArrayList;
import java.util.List;

public class InstalledThemeMemento implements Configuration {
	public List<String> lastLoadedThemePaths = new ArrayList<>();
	public List<String> lastInstalledThemeIds = new ArrayList<>();
}
