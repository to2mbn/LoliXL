package org.to2mbn.lolixl.ui.impl.pages.home;

import org.to2mbn.lolixl.ui.View;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class HomeContentView extends View {

	@FXML
	public BorderPane rootContainer;

	@FXML
	public BorderPane tileRootContainer;

	@FXML
	public VBox tileContainer;

	@FXML
	public AnchorPane bottomContainer;

	@FXML
	public Button startGameButton;

	@FXML
	private void initialize() {
		AnchorPane.setRightAnchor(startGameButton, 5.0);
		AnchorPane.setBottomAnchor(startGameButton, 5.0);
	}

}
