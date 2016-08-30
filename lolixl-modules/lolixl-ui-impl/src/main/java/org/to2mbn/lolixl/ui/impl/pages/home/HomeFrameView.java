package org.to2mbn.lolixl.ui.impl.pages.home;

import org.to2mbn.lolixl.ui.View;
import org.to2mbn.lolixl.utils.FXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public class HomeFrameView extends View {

	@FXML
	public StackPane rootContainer;

	@FXML
	public StackPane contentPane;

	@FXML
	public BorderPane mainContainer;

	public BorderPane homeContentPane;

	public BlurBackgroundPane backgroundPane;

	@FXML
	private void initialize() {
		homeContentPane = new BorderPane();

		rootContainer.sceneProperty().addListener((dummy, oldVal, newVal) -> {
			rootContainer.prefWidthProperty().unbind();
			rootContainer.prefHeightProperty().unbind();
			if (newVal != null) {
				rootContainer.prefWidthProperty().bind(newVal.widthProperty());
				rootContainer.prefHeightProperty().bind(newVal.heightProperty());
			}
		});
		FXUtils.setSizeToPref(rootContainer);

		StackPane.setAlignment(mainContainer, Pos.TOP_LEFT);
		FXUtils.bindPrefSize(mainContainer, rootContainer);
		FXUtils.setSizeToPref(mainContainer);

		BorderPane.setAlignment(contentPane, Pos.TOP_LEFT);
		contentPane.prefWidthProperty().bind(rootContainer.widthProperty());
		// TODO: if we use a customize title bar, remember to minus the height of title bar
		contentPane.prefHeightProperty().bind(rootContainer.heightProperty());
		FXUtils.setSizeToPref(contentPane);
	}

}
