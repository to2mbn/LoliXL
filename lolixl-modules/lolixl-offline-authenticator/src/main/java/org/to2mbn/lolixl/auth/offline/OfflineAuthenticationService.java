package org.to2mbn.lolixl.auth.offline;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.lolixl.core.game.auth.AuthenticationProfile;
import org.to2mbn.lolixl.core.game.auth.AuthenticationService;
import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.ui.component.Tile;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.image.Image;

@Service({ AuthenticationService.class })
@Component
@Properties({
		@Property(name = AuthenticationService.PROPERTY_AUTH_METHOD, value = "org.to2mbn.lolixl.auth.offline")
})
public class OfflineAuthenticationService implements AuthenticationService {

	@Override
	public ObservableStringValue getLocalizedName() {
		return I18N.localize("org.to2mbn.lolixl.auth.offline.name");
	}

	@Override
	public AuthenticationProfile<?> createProfile() {
		return new OfflineAuthenticationProfile();
	}

	@Override
	public ObservableObjectValue<Image> getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Tile createTile() {
		// TODO Auto-generated method stub
		return null;
	}

}
