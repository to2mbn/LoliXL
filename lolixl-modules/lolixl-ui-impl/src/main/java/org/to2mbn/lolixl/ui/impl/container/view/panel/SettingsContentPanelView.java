package org.to2mbn.lolixl.ui.impl.container.view.panel;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import org.to2mbn.lolixl.ui.container.view.View;

public class SettingsContentPanelView extends View {
	@FXML
	public BorderPane rootContainer;

	@FXML
	public ListView<String> categoryContainer;

	@FXML
	public ScrollPane contentContainer;
}
