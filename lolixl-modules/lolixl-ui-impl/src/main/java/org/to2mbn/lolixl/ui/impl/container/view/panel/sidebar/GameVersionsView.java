package org.to2mbn.lolixl.ui.impl.container.view.panel.sidebar;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.to2mbn.lolixl.ui.container.view.View;

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
