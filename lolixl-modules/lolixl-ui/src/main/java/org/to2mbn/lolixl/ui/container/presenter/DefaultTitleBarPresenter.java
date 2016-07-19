package org.to2mbn.lolixl.ui.container.presenter;

import javafx.beans.binding.Bindings;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.to2mbn.lolixl.ui.container.view.DefaultTitleBarView;
import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

public class DefaultTitleBarPresenter extends Presenter<DefaultTitleBarView> {

	private Consumer<MouseEvent> closeButtonListener;
	private Stage parentStage;

	@Override
	public void initialize(URL fxmlLocation) throws IOException {
		super.initialize(fxmlLocation);
		AnchorPane.setLeftAnchor(view.titleLabel, 0D);
		AnchorPane.setRightAnchor(view.buttonContainer, 0D);
		if (System.getProperties().containsKey("org.to2mbn.lolixl.version")) {
			view.titleLabel.setText("LoliXL " + System.getProperty("org.to2mbn.lolixl.version"));
		}
		view.minimizeButton.setOnMouseClicked(this::onMinimizeButtonClicked);
		view.closeButton.setOnMouseClicked(this::onCloseButtonClicked);
		view.rootContainer.idProperty().bind(Bindings
				.when(parentStage.focusedProperty())
				.then(view.rootContainer.idProperty().get().replace("-unfocused", ""))
				.otherwise(view.rootContainer.idProperty().get().concat("-unfocused")));
	}

	private void onCloseButtonClicked(MouseEvent event) {
		closeButtonListener.accept(event);
	}

	private void onMinimizeButtonClicked(MouseEvent event) {
		parentStage.hide();
	}

	public void setCloseButtonListener(Consumer<MouseEvent> closeButtonListener) {
		this.closeButtonListener = closeButtonListener;
	}

	public void setParentStage(Stage parentStage) {
		this.parentStage = parentStage;
	}

}
