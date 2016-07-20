package org.to2mbn.lolixl.auth.offline;

import java.util.concurrent.CompletableFuture;
import org.to2mbn.jmccc.auth.Authenticator;
import org.to2mbn.jmccc.auth.OfflineAuthenticator;
import org.to2mbn.lolixl.core.auth.AuthenticationProfile;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;

public class OfflineAuthenticationProfile implements AuthenticationProfile<OfflineAuthenticationMemento> {

	private String username;

	@Override
	public OfflineAuthenticationMemento store() {
		OfflineAuthenticationMemento memento = new OfflineAuthenticationMemento();
		memento.username = username;
		return memento;
	}

	@Override
	public void restore(OfflineAuthenticationMemento memento) {
		username = memento.username;
	}

	@Override
	public Class<? extends OfflineAuthenticationMemento> getMementoType() {
		return OfflineAuthenticationMemento.class;
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

	@Override
	public Button createTile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Region createConfiguringPanel() {
		// TODO Auto-generated method stub
		return null;
	}

}
