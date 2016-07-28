package org.to2mbn.lolixl.ui.impl.theme.management;

import org.to2mbn.lolixl.core.config.Configuration;

import java.util.ArrayList;
import java.util.List;

public class InstalledThemesConfiguration implements Configuration {
	public List<String> urls = new ArrayList<>();
	public String lastInstalledThemeId = "";
}
