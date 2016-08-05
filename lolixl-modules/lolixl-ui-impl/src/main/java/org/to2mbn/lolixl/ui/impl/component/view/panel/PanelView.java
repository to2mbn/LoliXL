package org.to2mbn.lolixl.ui.impl.component.view.panel;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.to2mbn.lolixl.ui.Panel;
import org.to2mbn.lolixl.utils.BundleUtils;
import org.to2mbn.lolixl.utils.FXUtils;
import java.io.IOException;

public class PanelView extends StackPane {
	private static final String LOCATION_OF_FXML = "/ui/fxml/panel/panel.fxml";

	private static final Image PREVIOUS_BUTTON_IMG = new Image("/ui/img/previous_button.png");
	private static final Image PREVIOUS_BUTTON_HOVER_IMG = new Image("/ui/img/previous_button_hover.png");

	@FXML
	public Pane glurContainer;

	@FXML
	public BorderPane contentContainer;

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
		previousButton.setImage(PREVIOUS_BUTTON_IMG); // TODO: css
		previousButton.setOnMouseMoved(event -> previousButton.setImage(PREVIOUS_BUTTON_HOVER_IMG));
		previousButton.setOnMouseExited(event -> previousButton.setImage(PREVIOUS_BUTTON_IMG));
		previousButton.setOnMouseClicked(event -> panel.hide());
		titleLabel.setLabelFor(iconView);
		titleLabel.textProperty().bind(panel.titleProperty());
		panel.contentProperty().addListener(((observable, oldValue, newValue) -> {
			if (oldValue != null) {
				panelContentContainer.getChildren().remove(oldValue);
			}
			panelContentContainer.getChildren().add(newValue);
		}));
		iconView.imageProperty().bind(panel.iconProperty());
		iconView.imageProperty().addListener(((observable, oldValue, newValue) -> checkEmptyIcon()));
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
