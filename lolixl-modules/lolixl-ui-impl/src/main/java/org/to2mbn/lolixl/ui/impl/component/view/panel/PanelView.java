package org.to2mbn.lolixl.ui.impl.component.view.panel;

import javafx.beans.value.ObservableObjectValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.to2mbn.lolixl.ui.ImageLoading;
import org.to2mbn.lolixl.ui.Panel;
import org.to2mbn.lolixl.utils.BundleUtils;
import org.to2mbn.lolixl.utils.FXUtils;
import java.io.IOException;
import java.io.UncheckedIOException;

public class PanelView extends BorderPane {

	private static final String FXML_LOCATION = "/ui/fxml/panel/panel.fxml";

	@FXML
	public HBox headerContainer;

	@FXML
	public ImageView previousButton;

	@FXML
	public ImageView iconView;

	@FXML
	public Label titleLabel;

	@FXML
	public StackPane panelContentContainer;

	private final Panel panel;

	public PanelView(Panel _panel) {
		panel = _panel;
		FXMLLoader loader = new FXMLLoader(BundleUtils.getResourceFromBundle(getClass(), FXML_LOCATION));
		loader.setRoot(this);
		loader.setController(this);
		try {
			loader.load();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		initComponent();
	}

	private void initComponent() {
		FXUtils.checkFxThread();

		ObservableObjectValue<Image> previous_button_img = ImageLoading.load("img/org.to2mbn.lolixl.ui/previous_button.png");
		ObservableObjectValue<Image> previous_button_hover_img = ImageLoading.load("img/org.to2mbn.lolixl.ui/previous_button_hover.png");

		previousButton.imageProperty().bind(previous_button_img);
		previousButton.setOnMouseMoved(event -> previousButton.imageProperty().bind(previous_button_hover_img));
		previousButton.setOnMouseExited(event -> previousButton.imageProperty().bind(previous_button_img));

		previousButton.setOnMouseClicked(event -> panel.hide());
		titleLabel.setLabelFor(iconView);
		titleLabel.textProperty().bind(panel.titleProperty());
		panel.contentProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue != null) {
				panelContentContainer.getChildren().remove(oldValue);
			}
			panelContentContainer.getChildren().add(newValue);
		});
		iconView.imageProperty().bind(panel.iconProperty());
		iconView.imageProperty().addListener((observable, oldValue, newValue) -> checkEmptyIcon());
		checkEmptyIcon();
	}

	private void checkEmptyIcon() {
		if (iconView.getImage() == null) {
			headerContainer.getChildren().remove(iconView);
		} else if (!headerContainer.getChildren().contains(iconView)) {
			headerContainer.getChildren().add(iconView);
		}
	}
}
