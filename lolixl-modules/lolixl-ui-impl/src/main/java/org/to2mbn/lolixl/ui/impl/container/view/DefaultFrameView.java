package org.to2mbn.lolixl.ui.impl.container.view;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.to2mbn.lolixl.ui.container.view.View;

public class DefaultFrameView extends View {
	@FXML
	public StackPane shadowContainer;

	@FXML
	public BorderPane rootContainer;

	@FXML
	public StackPane titleBarPane;

	@FXML
	public StackPane sidebarPane;

	@FXML
	public StackPane contentPane;
}
