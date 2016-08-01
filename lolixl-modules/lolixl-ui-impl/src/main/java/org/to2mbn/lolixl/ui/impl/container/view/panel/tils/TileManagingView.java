package org.to2mbn.lolixl.ui.impl.container.view.panel.tils;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.component.TileListCell;
import org.to2mbn.lolixl.ui.container.view.View;

public class TileManagingView extends View {
	@FXML
	public BorderPane rootContainer;

	@FXML
	public ListView<Tile> listView;

	@FXML
	public VBox buttonContainer;

	@FXML
	public Button upButton;

	@FXML
	public Button downButton;

	@FXML
	private void initialize() {
		listView.setCellFactory(view -> new TileListCell());
	}
}
