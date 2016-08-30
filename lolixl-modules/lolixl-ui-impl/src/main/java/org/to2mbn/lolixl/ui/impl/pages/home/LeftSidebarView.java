package org.to2mbn.lolixl.ui.impl.pages.home;

import org.to2mbn.lolixl.ui.View;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LeftSidebarView extends View {

	@FXML
	public BorderPane rootContainer;

	@FXML
	public BorderPane mainContentContainer;

	@FXML
	public StackPane userProfileContainer;

	@FXML
	public BorderPane functionalTileRootContainer;

	@FXML
	public StackPane functionalTileTopContainer;

	@FXML
	public VBox functionalTileCenterContainer;

	@FXML
	public StackPane functionalTileBottomContainer;

	@FXML
	public ScrollPane sidebarContainer;

	@FXML
	private void initialize() {
		sidebarContainer.maxWidthProperty().set(Region.USE_PREF_SIZE);
		sidebarContainer.minWidthProperty().set(Region.USE_PREF_SIZE);
	}
}
