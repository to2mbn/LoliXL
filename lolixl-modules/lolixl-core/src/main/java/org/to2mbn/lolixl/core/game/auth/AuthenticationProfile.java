package org.to2mbn.lolixl.core.game.auth;

import java.util.Map;
import org.to2mbn.jmccc.auth.Authenticator;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.Texture;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.TextureType;
import org.to2mbn.lolixl.ui.DisplayableTile;
import org.to2mbn.lolixl.utils.ObservableContextAware;
import org.to2mbn.lolixl.utils.Storable;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Region;

public interface AuthenticationProfile<MEMO extends java.io.Serializable> extends Storable<MEMO>, DisplayableTile, ObservableContextAware {

	Authenticator getAuthenticator();

	Region createConfiguringPanel();

	ObservableStringValue getUsername();

	default ObservableStringValue getEmail() {
		return null;
	}

	default ObservableValue<Map<TextureType, Texture>> getTextures() {
		return null;
	}

	@Override
	default ObservableStringValue getLocalizedName() {
		return getUsername();
	}

}
