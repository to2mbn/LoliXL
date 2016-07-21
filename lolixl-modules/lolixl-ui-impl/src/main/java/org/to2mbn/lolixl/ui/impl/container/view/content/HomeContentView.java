package org.to2mbn.lolixl.ui.impl.container.view.content;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.to2mbn.lolixl.ui.container.view.View;

public class HomeContentView extends View {
	@FXML
	public BorderPane rootContainer;

	@FXML
	public VBox tileContainer;

	@FXML
	public AnchorPane bottomContainer;

	@FXML
	public Button startGameButton;

	@FXML
	public Button settingsTile;

	@FXML
	public Button setTileSizeTile;
}
