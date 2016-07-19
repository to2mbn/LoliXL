package org.to2mbn.lolixl.ui;

import javafx.scene.control.Button;

public interface TileManagingService {
	void addTile(Button tileButton);

	void removeTile(Button tileButton);

	void changeSize(int size);
}
