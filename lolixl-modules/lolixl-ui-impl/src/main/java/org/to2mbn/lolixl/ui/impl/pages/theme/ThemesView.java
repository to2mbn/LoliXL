package org.to2mbn.lolixl.ui.impl.pages.theme;

import org.to2mbn.lolixl.ui.View;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;

public class ThemesView extends View {
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
