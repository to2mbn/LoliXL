package org.to2mbn.lolixl.auth.offline;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.lolixl.core.auth.AuthenticationProfile;
import org.to2mbn.lolixl.core.auth.AuthenticationService;
import org.to2mbn.lolixl.i18n.I18N;

@Service({ AuthenticationService.class })
@Component
@Properties({
		@Property(name = AuthenticationService.PROPERTY_AUTH_METHOD, value = "org.to2mbn.lolixl.auth.offline")
})
public class OfflineAuthenticationService implements AuthenticationService {

	@Override
	public String getLocalizedName() {
		return I18N.localize("org.to2mbn.lolixl.auth.offline.name");
	}

	@Override
	public AuthenticationProfile<?> createProfile() {
		return new OfflineAuthenticationProfile();
	}

}
