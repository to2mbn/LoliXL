package org.to2mbn.lolixl.ui.impl.theme.management;

import org.to2mbn.lolixl.core.config.Configuration;

import java.util.ArrayList;
import java.util.List;

public class InstalledThemeMemento implements Configuration {
	public List<String> lastLoadedThemePackageUrls = new ArrayList<>();
	public String lastInstalledThemeId = "";
}
