package org.to2mbn.lolixl.ui.impl.container.view.panel;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import org.to2mbn.lolixl.ui.container.view.View;

public class ThemesContentPanelView extends View {
	@FXML
	public BorderPane rootContainer;

	@FXML
	public GridPane themeInfoContainer;

	@FXML
	public FlowPane themesContainer;

	@FXML
	public Label themeNameLabel;

	@FXML
	public Label themeAuthorsLabel;

	@FXML
	public Label themeDescriptionLabel;
}
