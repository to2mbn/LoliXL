package org.to2mbn.lolixl.ui.impl.pages.theme;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.to2mbn.lolixl.utils.BundleUtils;
import java.io.IOException;
import java.io.UncheckedIOException;

public class ThemeTileView extends BorderPane {
	private static final String FXML_LOCATION = "/ui/fxml/component/theme_tile.fxml";

	@FXML
	public Label nameLabel;

	@FXML
	public ImageView iconView;

	public ThemeTileView() {
		FXMLLoader loader = new FXMLLoader(BundleUtils.getResourceFromBundle(getClass(), FXML_LOCATION));
		loader.setRoot(this);
		loader.setController(this);
		try {
			loader.load();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
