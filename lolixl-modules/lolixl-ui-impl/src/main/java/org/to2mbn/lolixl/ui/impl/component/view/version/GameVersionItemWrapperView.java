package org.to2mbn.lolixl.ui.impl.component.view.version;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import org.to2mbn.lolixl.utils.BundleUtils;
import java.io.IOException;
import java.io.UncheckedIOException;

public class GameVersionItemWrapperView extends BorderPane {
	private static final String FXML_LOCATION = "/ui/fxml/component/game_version_item_wrapper_item.fxml";

	@FXML
	public Button showButton;

	public GameVersionItemWrapperView() {
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
