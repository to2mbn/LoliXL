package org.to2mbn.lolixl.core.game.auth;

import java.util.List;
import org.osgi.framework.ServiceReference;

public interface AuthenticationProfileManager {

	List<AuthenticationProfile<?>> getProfiles();

	AuthenticationProfile<?> createProfile(ServiceReference<AuthenticationService> provider);

	void removeProfile(AuthenticationProfile<?> profile);
}
