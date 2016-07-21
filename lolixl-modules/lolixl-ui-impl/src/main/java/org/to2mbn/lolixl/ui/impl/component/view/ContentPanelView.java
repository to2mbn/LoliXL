package org.to2mbn.lolixl.ui.impl.component.view;

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

public class ContentPanelView extends BorderPane {
	private static final String LOCATION_OF_FXML = "/ui/fxml/panel/content_panel.fxml";

	@FXML
	private HBox headerContainer;

	@FXML
	private Button previousButton;

	@FXML
	private ImageView iconView;

	@FXML
	private Label titleLabel;

	@FXML
	private BorderPane paneContainer;

	private final Panel panel;

	public ContentPanelView(Panel _panel) throws IOException {
		panel = _panel;
		FXMLLoader loader = new FXMLLoader(BundleUtils.getResourceFromBundle(getClass(), LOCATION_OF_FXML));
		loader.setRoot(this);
		loader.setController(this);
		loader.load();
		initComponent();
	}

	private void initComponent() {
		FXUtils.checkFxThread();
		titleLabel.setLabelFor(previousButton);
		titleLabel.setText(panel.getTitle());
		iconView.setImage(panel.getIcon());
		paneContainer.setCenter(panel.getContent());
	}
}
