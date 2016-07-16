package org.to2mbn.lolixl.ui.container.presenter;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.lolixl.ui.container.view.DefaultUserProfileView;

import java.net.URL;

@Component
@Service({DefaultUserProfilePresenter.class})
public class DefaultUserProfilePresenter implements ViewInitializer {
	private DefaultUserProfileView view;
	private BorderPane root;

	@Override
	public void initialize(URL fxmlLocation) {
		view = new FXMLLoader(fxmlLocation).getController();
		root = view.rootContainer;
	}

	public DefaultUserProfileView getView() {
		return view;
	}

	public BorderPane getRoot() {
		return root;
	}
}
