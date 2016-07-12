package org.to2mbn.lolixl.ui.container.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.to2mbn.lolixl.ui.UIApp;

public class DefaultTitleBarView {
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

	@FXML
	public void onCloseButtonClicked(ActionEvent event) {
		// TODO
	}

	@FXML
	public void onMinimizeButtonClicked(ActionEvent event) {
		UIApp.mainStage.get().hide();
	}

	@FXML
	public void initialize() {
		rootContainer.setLeftAnchor(titleLabel, 0D);
		rootContainer.setRightAnchor(buttonContainer, 0D);

		if (System.getProperties().containsKey("org.to2mbn.lolixl.version")) {
			titleLabel.setText("LoliXL " + System.getProperty("org.to2mbn.lolixl.version"));
		}

	}
}
