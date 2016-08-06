package org.to2mbn.lolixl.ui.impl.auth;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.osgi.framework.ServiceReference;
import org.to2mbn.lolixl.core.config.Configuration;
import org.to2mbn.lolixl.core.game.auth.AuthenticationProfile;
import org.to2mbn.lolixl.core.game.auth.AuthenticationService;

public class AuthenticationProfileList implements Configuration {
	
	public static class AuthenticationProfileEntry implements Serializable {

		private static final long serialVersionUID = 1L;

		public String method;
		public UUID uuid;
		public volatile transient AuthenticationProfile<?> profile;
		public volatile transient ServiceReference<AuthenticationService> serviceRef;

		@Override
		public String toString() {
			return String.format("[method=%s, uuid=%s]", method, uuid);
		}

	}

	private static final long serialVersionUID = 1L;

	public List<AuthenticationProfileEntry> entries = new CopyOnWriteArrayList<>();
	public volatile UUID selectedProfile;

}
