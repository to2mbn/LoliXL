package org.to2mbn.lolixl.core.game.auth;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import org.osgi.framework.ServiceReference;
import java.util.Optional;

public interface AuthenticationProfileManager {

	ObservableList<AuthenticationProfile<?>> getProfiles();

	AuthenticationProfile<?> createProfile(ServiceReference<AuthenticationService> provider);

	Optional<ServiceReference<AuthenticationService>> getProfileProvider(AuthenticationProfile<?> profile);

	void removeProfile(AuthenticationProfile<?> profile);

	ObjectProperty<AuthenticationProfile> selectedProfileProperty();
}
