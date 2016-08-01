package org.to2mbn.lolixl.ui.impl.container.view.panel.downloads;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import org.to2mbn.lolixl.ui.container.view.View;

public class DownloadCenterView extends View {
	@FXML
	public ScrollPane rootContainer;

	@FXML
	public VBox itemContainer;

	@FXML
	private void initialize() {
		itemContainer.prefWidthProperty().bind(rootContainer.widthProperty());
	}
}
