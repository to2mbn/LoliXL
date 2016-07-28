package org.to2mbn.lolixl.ui.impl.container.presenter.panelcontent;

import org.to2mbn.lolixl.core.config.Configuration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TilesOrderConfiguration implements Configuration {

	private static final long serialVersionUID = 1L;

	public Map<String, Integer> tilesOrder = new ConcurrentHashMap<>();
}
