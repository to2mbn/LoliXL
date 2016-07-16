package org.to2mbn.lolixl.ui.container.presenter;

import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.event.EventAdmin;
import org.to2mbn.lolixl.ui.UIPrimaryReferenceProvider;
import org.to2mbn.lolixl.ui.container.view.DefaultTitleBarView;
import org.to2mbn.lolixl.utils.event.ApplicationExitEvent;

import java.io.IOException;
import java.net.URL;

@Component
@Service({DefaultTitleBarPresenter.class})
public class DefaultTitleBarPresenter implements ViewInitializer {
	@Reference
	private EventAdmin eventAdmin;

	@Reference
	private UIPrimaryReferenceProvider mainStageProvider;

	private DefaultTitleBarView view;
	private AnchorPane root;

	@Override
	public void initialize(URL fxmlLocation) throws IOException {
		view = new FXMLLoader(fxmlLocation).getController();
		root = view.rootContainer;
		view.minimizeButton.setOnMouseClicked(this::onMinimizeButtonClicked);
		view.closeButton.setOnMouseClicked(this::onCloseButtonClicked);
	}

	public DefaultTitleBarView getView() {
		return view;
	}

	public AnchorPane getRoot() {
		return root;
	}

	private void onCloseButtonClicked(MouseEvent event) {
		eventAdmin.postEvent(new ApplicationExitEvent());
	}

	private void onMinimizeButtonClicked(MouseEvent event) {
		mainStageProvider.getMainStage().hide();
	}
}
