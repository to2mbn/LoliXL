package org.to2mbn.lolixl.ui.impl.component.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.to2mbn.lolixl.ui.theme.Theme;
import org.to2mbn.lolixl.utils.BundleUtils;
import java.io.IOException;

public class ThemeTileView extends StackPane {
	private static final String FXML_LOCATION = "/ui/fxml/component/theme_tile.fxml";

	@FXML
	public Pane blurContainer;

	@FXML
	public BorderPane contentContainer;

	@FXML
	public Label nameLabel;

	@FXML
	public ImageView iconView;

	public ThemeTileView(Theme theme) throws IOException {
		FXMLLoader loader = new FXMLLoader(BundleUtils.getResourceFromBundle(getClass(), FXML_LOCATION));
		loader.setRoot(this);
		loader.setController(this);
		loader.load();
		nameLabel.textProperty().bind(theme.getLocalizedName());
		iconView.imageProperty().bind(theme.getIcon());
	}
}
