package org.to2mbn.lolixl.core.auth;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.to2mbn.jmccc.auth.Authenticator;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.Texture;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.TextureType;
import org.to2mbn.lolixl.utils.Storable;

public interface AuthenticationProfile<MEMO extends java.io.Serializable> extends Storable<MEMO> {

	Authenticator getAuthenticator();

	CompletableFuture<String> getUsername();

	default CompletableFuture<String> getEmail() {
		return CompletableFuture.completedFuture(null);
	}

	default CompletableFuture<Map<TextureType, Texture>> getTextures() {
		return CompletableFuture.completedFuture(null);
	}

}
