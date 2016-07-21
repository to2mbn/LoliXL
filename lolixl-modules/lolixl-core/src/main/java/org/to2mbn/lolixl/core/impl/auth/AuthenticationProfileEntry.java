package org.to2mbn.lolixl.core.impl.auth;

import java.io.Serializable;
import java.util.UUID;
import org.osgi.framework.ServiceReference;
import org.to2mbn.lolixl.core.game.auth.AuthenticationProfile;
import org.to2mbn.lolixl.core.game.auth.AuthenticationService;

public class AuthenticationProfileEntry implements Serializable {

	private static final long serialVersionUID = 1L;

	public String method;
	public UUID uuid;
	public transient AuthenticationProfile<?> profile;
	public transient ServiceReference<AuthenticationService> serviceRef;

}
