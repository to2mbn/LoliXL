package org.to2mbn.lolixl.ui.impl.pages.download;

import org.to2mbn.lolixl.ui.View;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class DownloadCenterView extends View {
	@FXML
	public ScrollPane rootContainer;

	@FXML
	public VBox itemsContainer;

	@FXML
	private void initialize() {
		itemsContainer.prefWidthProperty().bind(rootContainer.widthProperty());
		VBox.setVgrow(itemsContainer, Priority.ALWAYS);
	}
}
