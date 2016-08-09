package org.to2mbn.lolixl.ui.impl.component.view.version;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.to2mbn.lolixl.utils.BundleUtils;
import java.io.IOException;
import java.io.UncheckedIOException;

public class GameVersionEditorView extends GridPane {
	private static final String FXML_LOCATION = "/ui/fxml/component/game_version_editor.fxml";

	@FXML
	public Label mcdirPathLabel;

	@FXML
	public Label versionNumberLabel;

	@FXML
	public Label aliasLabel;

	@FXML
	public Label tagsLabel;

	@FXML
	public Label releaseTimeLabel;

	@FXML
	public Label mcdirPathContentLabel;

	@FXML
	public Label versionNumberContentLabel;

	@FXML
	public Label releaseTimeContentLabel;

	@FXML
	public HBox tagsContainer;

	@FXML
	public TextField aliasInput;

	public GameVersionEditorView() {
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
