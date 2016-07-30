package org.to2mbn.lolixl.ui.impl.theme;

import org.to2mbn.lolixl.core.config.Configuration;
import org.to2mbn.lolixl.ui.theme.Theme;
import java.io.Serializable;
import java.util.LinkedHashSet;

public class ThemeMemento implements Configuration {

	private static final long serialVersionUID = 1L;

	public static class ThemeEntry implements Serializable {

		private static final long serialVersionUID = 1L;

		public String id;
		public boolean enabled;
		public volatile transient Theme theme;

	}

	public LinkedHashSet<ThemeEntry> themes;

}
