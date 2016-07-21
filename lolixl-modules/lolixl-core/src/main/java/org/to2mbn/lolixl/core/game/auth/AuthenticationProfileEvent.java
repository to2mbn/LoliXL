package org.to2mbn.lolixl.core.game.auth;

import java.util.HashMap;
import java.util.Map;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;

public class AuthenticationProfileEvent extends Event {

	public static final int TYPE_UPDATE = 1;
	public static final int TYPE_CREATE = 2;
	public static final int TYPE_DELETE = 3;

	public static final String TOPIC_AUTH_PROFILE = "org/to2mbn/lolixl/core/game/auth/profileChanged";

	public static final String KEY_TYPE = "org.to2mbn.lolixl.core.game.auth.profileChanged.type";
	public static final String KEY_PROFILE = "org.to2mbn.lolixl.core.game.auth.profile";
	public static final String KEY_PROVIDER_REF = "org.to2mbn.lolixl.core.game.auth.profile.providerRef";

	private static Map<String, Object> createProperties(int type, AuthenticationProfile<?> profile, ServiceReference<AuthenticationService> providerRef) {
		Map<String, Object> properties = new HashMap<>();
		properties.put(KEY_TYPE, type);
		properties.put(KEY_PROFILE, profile);
		properties.put(KEY_PROVIDER_REF, providerRef);
		return properties;
	}

	private int type;
	private AuthenticationProfile<?> profile;
	private ServiceReference<AuthenticationService> providerReference;

	public AuthenticationProfileEvent(int type, AuthenticationProfile<?> profile, ServiceReference<AuthenticationService> providerReference) {
		super(TOPIC_AUTH_PROFILE, createProperties(type, profile, providerReference));
		this.type = type;
		this.profile = profile;
		this.providerReference = providerReference;
	}

	public int getType() {
		return type;
	}

	public AuthenticationProfile<?> getProfile() {
		return profile;
	}

	public ServiceReference<AuthenticationService> getProviderReference() {
		return providerReference;
	}

}
