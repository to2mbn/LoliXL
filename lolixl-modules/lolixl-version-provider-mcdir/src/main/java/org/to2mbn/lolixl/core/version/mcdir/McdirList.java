package org.to2mbn.lolixl.core.version.mcdir;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.osgi.framework.ServiceRegistration;
import org.to2mbn.lolixl.core.config.Configuration;

public class McdirList implements Configuration {

	public static class McdirEntry implements Serializable {

		private static final long serialVersionUID = 1L;

		public String path;
		public transient McdirGameVersionProvider service;
		public transient ServiceRegistration<?> registration;

	}

	private static final long serialVersionUID = 1L;

	public Set<McdirEntry> mcdirs = Collections.synchronizedSet(new LinkedHashSet<>());

}
