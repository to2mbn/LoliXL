package org.to2mbn.lolixl.core.auth;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.to2mbn.jmccc.auth.Authenticator;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.Texture;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.TextureType;

public interface AuthenticationProfile<CONFIG extends Serializable> {

	Authenticator getAuthenticator();

	CompletableFuture<String> getUsername();

	default CompletableFuture<String> getEmail() {
		return CompletableFuture.completedFuture(null);
	}

	default CompletableFuture<Map<TextureType, Texture>> getTextures() {
		return CompletableFuture.completedFuture(null);
	}

	CONFIG getConfiguration();

}
