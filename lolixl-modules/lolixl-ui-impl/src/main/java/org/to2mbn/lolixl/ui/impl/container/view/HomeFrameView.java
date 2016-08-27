package org.to2mbn.lolixl.ui.impl.container.view;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.to2mbn.lolixl.ui.container.view.View;

public class HomeFrameView extends View {

	@FXML
	public BorderPane rootContainer;

	@FXML
	public StackPane contentPane;

	public BorderPane homeContentPane;

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
		rootContainer.setMaxWidth(Region.USE_PREF_SIZE);
		rootContainer.setMinWidth(Region.USE_PREF_SIZE);
		rootContainer.setMaxHeight(Region.USE_PREF_SIZE);
		rootContainer.setMinHeight(Region.USE_PREF_SIZE);

		BorderPane.setAlignment(contentPane, Pos.TOP_LEFT);
		contentPane.prefWidthProperty().bind(rootContainer.widthProperty());
		// TODO: if we use a customize title bar, remember to minus the height of title bar
		contentPane.prefHeightProperty().bind(rootContainer.heightProperty());
		contentPane.setMaxWidth(Region.USE_PREF_SIZE);
		contentPane.setMinWidth(Region.USE_PREF_SIZE);
		contentPane.setMaxHeight(Region.USE_PREF_SIZE);
		contentPane.setMinHeight(Region.USE_PREF_SIZE);
	}

}
