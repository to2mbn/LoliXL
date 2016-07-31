package org.to2mbn.lolixl.ui.impl.container.view.panel;

import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.to2mbn.lolixl.core.config.ConfigurationCategory;
import org.to2mbn.lolixl.ui.container.view.View;

public class SettingsPanelContentView extends View {
	@FXML
	public BorderPane rootContainer;

	@FXML
	public ListView<ConfigurationCategory> categoryContainer;

	@FXML
	public StackPane contentContainer;

	@FXML
	private void initialize() {
		categoryContainer.setCellFactory(list -> new ListCell<ConfigurationCategory>() {
			@Override
			public void updateItem(ConfigurationCategory category, boolean empty) {
				super.updateItem(category, empty);
				setText(category.getLocalizedName());
				setGraphic(new ImageView(category.getIcon()));
			}
		});
	}
}
