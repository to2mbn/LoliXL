package org.to2mbn.lolixl.core.game.auth;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.to2mbn.jmccc.auth.Authenticator;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.Texture;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.TextureType;
import org.to2mbn.lolixl.ui.model.DisplayableTile;
import org.to2mbn.lolixl.utils.Observable;
import org.to2mbn.lolixl.utils.Storable;
import javafx.scene.layout.Region;

public interface AuthenticationProfile<MEMO extends java.io.Serializable> extends Storable<MEMO>, DisplayableTile, Observable {

	Authenticator getAuthenticator();

	CompletableFuture<String> getUsername();

	Region createConfiguringPanel();

	default CompletableFuture<String> getEmail() {
		return CompletableFuture.completedFuture(null);
	}

	default CompletableFuture<Map<TextureType, Texture>> getTextures() {
		return CompletableFuture.completedFuture(null);
	}

}
