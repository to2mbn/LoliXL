package org.to2mbn.lolixl.ui.impl.container.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.to2mbn.lolixl.ui.container.view.View;

public class DefaultTitleBarView extends View {
	@FXML
	public AnchorPane rootContainer;

	@FXML
	public Label titleLabel;

	@FXML
	public HBox buttonContainer;

	@FXML
	public ImageView minimizeButton;

	@FXML
	public ImageView closeButton;
}
