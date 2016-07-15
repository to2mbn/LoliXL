package org.to2mbn.lolixl.ui.service;

import javafx.scene.layout.Pane;

import java.util.List;

public interface DisplayService {
	List<Pane> getAvailablePanes();

	void displayPane(Pane pane);

	boolean closeCurrentPane();

	void addListener(ContentPaneListener listener);

	void removeListener(ContentPaneListener listener);
}
