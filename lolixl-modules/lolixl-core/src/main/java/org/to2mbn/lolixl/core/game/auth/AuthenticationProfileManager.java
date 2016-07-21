package org.to2mbn.lolixl.core.game.auth;

import java.util.List;
import java.util.Optional;
import org.osgi.framework.ServiceReference;

public interface AuthenticationProfileManager {

	List<AuthenticationProfile<?>> getProfiles();

	AuthenticationProfile<?> createProfile(ServiceReference<AuthenticationService> provider);

	Optional<ServiceReference<AuthenticationService>> getProfileProvider(AuthenticationProfile<?> profile);

	void removeProfile(AuthenticationProfile<?> profile);
}
