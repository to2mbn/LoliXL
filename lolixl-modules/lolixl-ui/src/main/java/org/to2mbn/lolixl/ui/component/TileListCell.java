package org.to2mbn.lolixl.ui.component;

import javafx.scene.control.ListCell;

public class TileListCell extends ListCell<Tile> {

	@Override
	public void updateItem(Tile tile, boolean empty) {
		super.updateItem(tile, empty);

		if (empty || tile == null) {
			setText(null);
			setGraphic(null);
		} else {
			setGraphic(tile.getGraphic());
			setText(tile.getText());
		}
	}
}
