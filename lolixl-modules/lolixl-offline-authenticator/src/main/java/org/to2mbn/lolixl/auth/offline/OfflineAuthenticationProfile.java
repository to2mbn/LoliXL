package org.to2mbn.lolixl.auth.offline;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.layout.Region;
import org.to2mbn.jmccc.auth.Authenticator;
import org.to2mbn.jmccc.auth.OfflineAuthenticator;
import org.to2mbn.lolixl.core.game.auth.AuthenticationProfile;
import org.to2mbn.lolixl.ui.component.view.auth.OfflineAuthProfileView;
import org.to2mbn.lolixl.utils.ObservableContext;
import java.util.Optional;

public class OfflineAuthenticationProfile implements AuthenticationProfile<OfflineAuthenticationMemento> {

	private StringProperty username = new SimpleStringProperty();

	@Override
	public OfflineAuthenticationMemento store() {
		OfflineAuthenticationMemento memento = new OfflineAuthenticationMemento();
		memento.username = username.get();
		return memento;
	}

	@Override
	public void restore(Optional<OfflineAuthenticationMemento> optionalMemento) {
		optionalMemento.ifPresent(memento -> username.set(memento.username));
	}

	@Override
	public Class<? extends OfflineAuthenticationMemento> getMementoType() {
		return OfflineAuthenticationMemento.class;
	}

	@Override
	public Authenticator getAuthenticator() {
		return new OfflineAuthenticator(username.get());
	}

	@Override
	public ObservableStringValue getUsername() {
		return username;
	}

	@Override
	public Region createConfiguringPanel() {
		OfflineAuthProfileView view = null;
		view = new OfflineAuthProfileView();
		// TODO: bind avatar view
		username.bind(view.userNameInput.textProperty());
		// TODO: avatar path???
		return view;
	}

	@Override
	public void setObservableContext(ObservableContext ctx) {
		// TODO Auto-generated method stub
	}
}
