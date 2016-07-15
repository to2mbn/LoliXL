package org.to2mbn.lolixl.ui.service;

import javafx.scene.layout.Pane;

public interface DisplayPanesListener {
	void onPaneAdded(Pane pane);

	void onPaneRemoved(Pane paneRemoved, Pane previousPane);
}
