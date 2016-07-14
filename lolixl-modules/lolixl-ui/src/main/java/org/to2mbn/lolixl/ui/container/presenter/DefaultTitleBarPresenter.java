package org.to2mbn.lolixl.ui.container.presenter;

import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.apache.felix.scr.annotations.*;
import org.osgi.service.event.EventAdmin;
import org.to2mbn.lolixl.ui.UIPrimaryReferenceProvider;
import org.to2mbn.lolixl.utils.event.ApplicationExitEvent;

import java.io.IOException;
import java.net.URL;

@Component
@Service({Presenter.class})
@Properties({
		@Property(name = "presenter.name", value = "default_title_bar_presenter")
})
public class DefaultTitleBarPresenter extends Presenter {
	@Reference
	private EventAdmin eventAdmin;

	@Reference
	private UIPrimaryReferenceProvider mainStageProvider;

	@Override
	public void initialize(URL fxmlLocation) throws IOException {
		super.initialize(fxmlLocation);
		view.get().getComponent(ImageView.class, "minimizeButton").setOnMouseClicked(this::onMinimizeButtonClicked);
		view.get().getComponent(ImageView.class, "closeButton").setOnMouseClicked(this::onCloseButtonClicked);
	}

	private void onCloseButtonClicked(MouseEvent event) {
		eventAdmin.postEvent(new ApplicationExitEvent());
	}

	private void onMinimizeButtonClicked(MouseEvent event) {
		mainStageProvider.getMainStage().hide();
	}
}
