package org.to2mbn.lolixl.ui.impl.pages.version;

import org.to2mbn.lolixl.ui.View;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class GameVersionsView extends View {
	@FXML
	public BorderPane rootContainer;

	@FXML
	public StackPane topContainer;

	@FXML
	public ScrollPane contentContainer;

	@FXML
	public VBox versionsContainer;
}
