package org.to2mbn.lolixl.ui.impl.container.presenter.panel.tiles;

import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.ui.container.view.View;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;

public class TileManagementView extends View {

	public static final String CSS_CLASS_TILES_LABEL = "xl-tiles-management-tiles-label";

	@FXML
	public BorderPane rootContainer;

	@FXML
	public FlowPane tilesContainer;

	public Label shownTilesLabel;

	public Label hiddenTileLabel;

	@FXML
	private void initialize() {
		shownTilesLabel = new Label();
		shownTilesLabel.getStyleClass().add(CSS_CLASS_TILES_LABEL);
		shownTilesLabel.textProperty().bind(I18N.localize("org.to2mbn.lolixl.ui.tiles_management.tiles_label.shown"));

		hiddenTileLabel = new Label();
		hiddenTileLabel.getStyleClass().add(CSS_CLASS_TILES_LABEL);
		hiddenTileLabel.textProperty().bind(I18N.localize("org.to2mbn.lolixl.ui.tiles_management.tiles_label.hidden"));
	}

}
