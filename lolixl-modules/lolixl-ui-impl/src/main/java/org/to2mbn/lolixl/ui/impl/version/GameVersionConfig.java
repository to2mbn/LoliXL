package org.to2mbn.lolixl.ui.impl.version;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.to2mbn.lolixl.core.config.Configuration;

public class GameVersionConfig implements Configuration {

	private static final long serialVersionUID = 1L;

	public static class GameVersionProviderEntry implements Serializable {

		private static final long serialVersionUID = 1L;

		volatile String alias;
		ConcurrentMap<String, String> versionAlias = new ConcurrentHashMap<>();

	}

	volatile SelectedGameVersion selected;
	ConcurrentMap<String, GameVersionProviderEntry> providers = new ConcurrentHashMap<>();

}
