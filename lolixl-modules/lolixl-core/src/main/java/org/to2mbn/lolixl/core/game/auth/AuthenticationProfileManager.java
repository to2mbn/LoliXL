package org.to2mbn.lolixl.core.game.auth;

import java.util.Optional;
import org.osgi.framework.ServiceReference;
import javafx.collections.ObservableList;

public interface AuthenticationProfileManager {

	ObservableList<AuthenticationProfile<?>> getProfiles();

	AuthenticationProfile<?> createProfile(ServiceReference<AuthenticationService> provider);

	Optional<ServiceReference<AuthenticationService>> getProfileProvider(AuthenticationProfile<?> profile);

	void removeProfile(AuthenticationProfile<?> profile);
}
