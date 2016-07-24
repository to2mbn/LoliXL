package org.to2mbn.lolixl.ui.impl.container.presenter.panelcontent;

import com.google.gson.internal.LinkedTreeMap;
import org.to2mbn.lolixl.core.config.Configuration;

import java.util.Map;

public class TilesOrderConfiguration implements Configuration {
	public Map<Integer, String> tiles = new LinkedTreeMap<>();
}
