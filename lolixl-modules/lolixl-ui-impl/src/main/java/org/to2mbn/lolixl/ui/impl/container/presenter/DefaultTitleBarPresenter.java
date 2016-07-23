package org.to2mbn.lolixl.ui.impl.container.presenter;

import javafx.beans.binding.Bindings;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.view.DefaultTitleBarView;

import java.util.function.Consumer;

public class DefaultTitleBarPresenter extends Presenter<DefaultTitleBarView> {

	private static final String LOCATION_OF_FXML = "/ui/fxml/container/default_title_bar.fxml";

	private Consumer<MouseEvent> closeButtonListener;
	private Stage parentStage;

	@Override
	public void postInitialize() {
		view.minimizeButton.setOnMouseClicked(this::onMinimizeButtonClicked);
		view.closeButton.setOnMouseClicked(this::onCloseButtonClicked);
		view.rootContainer.idProperty().bind(Bindings
				.when(parentStage.focusedProperty())
				.then(view.rootContainer.idProperty().get().replace("-unfocused", ""))
				.otherwise(view.rootContainer.idProperty().get().concat("-unfocused")));
		makeDraggable();
	}

	private void onCloseButtonClicked(MouseEvent event) {
		closeButtonListener.accept(event);
	}

	private void onMinimizeButtonClicked(MouseEvent event) {
		parentStage.hide();
	}

	private void makeDraggable() {
		// TODO
	}

	public void setCloseButtonListener(Consumer<MouseEvent> closeButtonListener) {
		this.closeButtonListener = closeButtonListener;
	}

	public void setParentStage(Stage parentStage) {
		this.parentStage = parentStage;
	}

	@Override
	protected String getFxmlLocation() {
		return LOCATION_OF_FXML;
	}
}
