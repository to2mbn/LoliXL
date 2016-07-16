package org.to2mbn.lolixl.ui.container.presenter;

import javafx.beans.binding.Bindings;
import javafx.scene.input.MouseEvent;
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
public class DefaultTitleBarPresenter extends Presenter<DefaultTitleBarView> {
	@Reference
	private EventAdmin eventAdmin;

	@Reference
	private UIPrimaryReferenceProvider mainStageProvider;

	@Override
	public void initialize(URL fxmlLocation) throws IOException {
		super.initialize(fxmlLocation);
		view.minimizeButton.setOnMouseClicked(this::onMinimizeButtonClicked);
		view.closeButton.setOnMouseClicked(this::onCloseButtonClicked);
		view.rootContainer.idProperty().bind(Bindings
				.when(mainStageProvider.getMainStage().focusedProperty())
				.then(view.rootContainer.idProperty().get().replace("-unfocused", ""))
				.otherwise(view.rootContainer.idProperty().get().concat("-unfocused")));
	}

	private void onCloseButtonClicked(MouseEvent event) {
		eventAdmin.postEvent(new ApplicationExitEvent());
	}

	private void onMinimizeButtonClicked(MouseEvent event) {
		mainStageProvider.getMainStage().hide();
	}
}
