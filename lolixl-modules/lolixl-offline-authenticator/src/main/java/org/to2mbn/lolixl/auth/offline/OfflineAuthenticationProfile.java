package org.to2mbn.lolixl.auth.offline;

import java.util.concurrent.CompletableFuture;
import org.to2mbn.jmccc.auth.Authenticator;
import org.to2mbn.jmccc.auth.OfflineAuthenticator;
import org.to2mbn.lolixl.core.auth.AuthenticationProfile;

public class OfflineAuthenticationProfile implements AuthenticationProfile<String> {

	private String username;

	@Override
	public String store() {
		return username;
	}

	@Override
	public void restore(String memento) {
		username = memento;
	}

	@Override
	public Class<? extends String> getMementoType() {
		return String.class;
	}

	@Override
	public Authenticator getAuthenticator() {
		return new OfflineAuthenticator(username);
	}

	@Override
	public CompletableFuture<String> getUsername() {
		return CompletableFuture.completedFuture(username);
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
