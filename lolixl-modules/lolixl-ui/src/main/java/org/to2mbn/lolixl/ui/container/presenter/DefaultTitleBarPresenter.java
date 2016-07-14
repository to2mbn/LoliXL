package org.to2mbn.lolixl.ui.container.presenter;

import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.service.event.EventAdmin;
import org.to2mbn.lolixl.ui.UIApp;
import org.to2mbn.lolixl.ui.api.ViewInitializer;
import org.to2mbn.lolixl.ui.container.view.DefaultTitleBarView;
import org.to2mbn.lolixl.utils.LazyReference;
import org.to2mbn.lolixl.utils.event.ApplicationExitEvent;

import java.io.IOException;
import java.net.URL;

@Component
public class DefaultTitleBarPresenter implements ViewInitializer {
	@Reference
	private EventAdmin eventAdmin;

	public final LazyReference<DefaultTitleBarView> view = new LazyReference<>();
	public final LazyReference<AnchorPane> root = new LazyReference<>();

	@Override

	public void initialize(URL fxmlLocation) throws IOException {
		view.set(new FXMLLoader(fxmlLocation).getController());
		root.set(view.get().rootContainer);
		view.get().minimizeButton.setOnMouseClicked(this::onMinimizeButtonClicked);
		view.get().closeButton.setOnMouseClicked(this::onCloseButtonClicked);
	}

	private void onCloseButtonClicked(MouseEvent event) {
		eventAdmin.postEvent(new ApplicationExitEvent());
	}

	private void onMinimizeButtonClicked(MouseEvent event) {
		UIApp.mainStage.get().hide();
	}
}
