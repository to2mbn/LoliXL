package org.to2mbn.lolixl.ui.impl.pages.settings;

import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.to2mbn.lolixl.ui.View;
import org.to2mbn.lolixl.ui.config.ConfigurationCategoryViewProvider;

public class SettingsView extends View {
	@FXML
	public BorderPane rootContainer;

	@FXML
	public ListView<ConfigurationCategoryViewProvider> categoryContainer;

	@FXML
	public StackPane contentContainer;

	@FXML
	private void initialize() {
		categoryContainer.setCellFactory(list -> new ListCell<ConfigurationCategoryViewProvider>() {
			@Override
			public void updateItem(ConfigurationCategoryViewProvider category, boolean empty) {
				super.updateItem(category, empty);
				textProperty().bind(category.getLocalizedName());
				ImageView iconView = new ImageView();
				iconView.imageProperty().bind(category.getIcon());
				setGraphic(iconView);
			}
		});
	}
}
