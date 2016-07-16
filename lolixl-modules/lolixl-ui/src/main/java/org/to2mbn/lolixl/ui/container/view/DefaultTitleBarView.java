package org.to2mbn.lolixl.ui.container.view;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.to2mbn.lolixl.ui.UIPrimaryReferenceProvider;

@Component
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

	@Reference
	private UIPrimaryReferenceProvider mainStageProvider;

	@FXML
	public void initialize() {
		AnchorPane.setLeftAnchor(titleLabel, 0D);
		AnchorPane.setRightAnchor(buttonContainer, 0D);

		if (System.getProperties().containsKey("org.to2mbn.lolixl.version")) {
			titleLabel.setText("LoliXL " + System.getProperty("org.to2mbn.lolixl.version"));
		}

		rootContainer.idProperty().bind(Bindings
				.when(mainStageProvider.getMainStage().focusedProperty())
				.then(rootContainer.idProperty().get().replace("-unfocused", ""))
				.otherwise(rootContainer.idProperty().get().concat("-unfocused")));
	}
}
