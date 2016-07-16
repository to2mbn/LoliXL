package org.to2mbn.lolixl.ui.container.presenter;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.lolixl.ui.container.view.DefaultFrameView;

import java.io.IOException;
import java.net.URL;

@Component
@Service({DefaultFramePresenter.class})
public class DefaultFramePresenter implements ViewInitializer {
	private DefaultFrameView view;
	private BorderPane root;

	@Override
	public void initialize(URL fxmlLocation) throws IOException {
		view = new FXMLLoader(fxmlLocation).getController();
		root = view.containerPane;
	}

	public DefaultFrameView getView() {
		return view;
	}

	public BorderPane getRoot() {
		return root;
	}
}
