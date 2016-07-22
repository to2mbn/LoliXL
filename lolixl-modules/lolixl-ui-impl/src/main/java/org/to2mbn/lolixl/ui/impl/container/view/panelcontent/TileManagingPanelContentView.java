package org.to2mbn.lolixl.ui.impl.container.view.panelcontent;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.to2mbn.lolixl.ui.container.view.View;

public class TileManagingPanelContentView extends View {
	@FXML
	private GridPane rootContainer;

	@FXML
	private ListView listView;

	@FXML
	private VBox buttonContainer;

	@FXML
	private Button upButton;

	@FXML
	private Button downButton;
}
