package org.to2mbn.lolixl.ui.impl.container.view.panel.settings;

import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.to2mbn.lolixl.core.config.ConfigurationCategory;
import org.to2mbn.lolixl.ui.container.view.View;

public class SettingsView extends View {
	@FXML
	public BorderPane rootContainer;

	@FXML
	public ListView<ConfigurationCategory<?>> categoryContainer;

	@FXML
	public StackPane contentContainer;

	@FXML
	private void initialize() {
		categoryContainer.setCellFactory(list -> new ListCell<ConfigurationCategory<?>>() {
			@Override
			public void updateItem(ConfigurationCategory<?> category, boolean empty) {
				super.updateItem(category, empty);
				textProperty().bind(category.getLocalizedName());
				ImageView iconView = new ImageView();
				iconView.imageProperty().bind(category.getIcon());
				setGraphic(iconView);
			}
		});
	}
}
