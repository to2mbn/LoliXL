package org.to2mbn.lolixl.ui.impl.component.view.panel;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.to2mbn.lolixl.ui.Panel;
import org.to2mbn.lolixl.utils.BundleUtils;
import org.to2mbn.lolixl.utils.FXUtils;
import java.io.IOException;

public class PanelView extends BorderPane {
	private static final String LOCATION_OF_FXML = "/ui/fxml/panel/panel.fxml";

	@FXML
	public HBox headerContainer;

	@FXML
	public Button previousButton;

	@FXML
	public ImageView iconView;

	@FXML
	public Label titleLabel;

	@FXML
	public BorderPane paneContainer;

	private final Panel panel;

	public PanelView(Panel _panel) throws IOException {
		panel = _panel;
		FXMLLoader loader = new FXMLLoader(BundleUtils.getResourceFromBundle(getClass(), LOCATION_OF_FXML));
		loader.setRoot(this);
		loader.setController(this);
		loader.load();
		initComponent();
	}

	private void initComponent() {
		FXUtils.checkFxThread();
		titleLabel.setLabelFor(iconView);
		titleLabel.textProperty().bind(panel.titleProperty());
		paneContainer.centerProperty().bind(panel.contentProperty());
		iconView.imageProperty().bind(panel.iconProperty());
		iconView.imageProperty().addListener(((observable, oldValue, newValue) -> checkEmptyIcon()));
		checkEmptyIcon();
		previousButton.setOnAction(event -> panel.hide());
	}

	private void checkEmptyIcon() {
		if (iconView.getImage() == null) {
			paneContainer.getChildren().remove(iconView);
		} else if (!paneContainer.getChildren().contains(iconView)) {
			paneContainer.getChildren().add(iconView);
		}
	}
}
