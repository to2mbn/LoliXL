package org.to2mbn.lolixl.auth.offline;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import org.to2mbn.jmccc.auth.Authenticator;
import org.to2mbn.jmccc.auth.OfflineAuthenticator;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.Texture;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.TextureType;
import org.to2mbn.lolixl.core.game.auth.AuthenticationProfile;
import org.to2mbn.lolixl.utils.ObservableContext;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

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

	@Override
	public CompletableFuture<String> getEmail() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Map<TextureType, Texture>> getTextures() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalizedName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Image getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setObservableContext(ObservableContext ctx) {
		// TODO Auto-generated method stub
	}
}
