package org.to2mbn.lolixl.ui.impl.container.view;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.to2mbn.lolixl.ui.container.view.View;

public class DefaultFrameView extends View {
	@FXML
	public Pane shadowContainer;

	@FXML
	public BorderPane rootContainer;

	@FXML
	public BorderPane titleBarPane;

	@FXML
	public BorderPane sidebarPane;

	@FXML
	public BorderPane contentPane;
}
