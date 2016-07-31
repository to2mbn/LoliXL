package org.to2mbn.lolixl.ui.impl.theme;

import org.osgi.framework.ServiceReference;
import org.to2mbn.lolixl.core.config.Configuration;
import org.to2mbn.lolixl.ui.theme.Theme;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class ThemeConfiguration implements Configuration {

	private static final long serialVersionUID = 1L;

	public static class ThemeEntry implements Serializable {

		private static final long serialVersionUID = 1L;

		public String id;
		public boolean enabled;
		public volatile transient Theme theme;
		public volatile transient ServiceReference<Theme> serviceRef;

	}

	public Set<ThemeEntry> themes = Collections.synchronizedSet(new LinkedHashSet<>());

}
