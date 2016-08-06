package org.to2mbn.lolixl.ui.impl.component.view.auth;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.utils.BundleUtils;
import java.io.IOException;
import java.io.UncheckedIOException;

public class AddNewProfileTileView extends BorderPane {
	private static final String FXML_LOCATION = "/ui/fxml/component/add_new_profile_tile.fxml";

	@FXML
	public ImageView iconView;

	@FXML
	public Label textLabel;

	public AddNewProfileTileView() {
		FXMLLoader loader = new FXMLLoader(BundleUtils.getResourceFromBundle(getClass(), FXML_LOCATION));
		loader.setRoot(this);
		loader.setController(this);
		try {
			loader.load();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		iconView.setImage(null); // TODO
		textLabel.textProperty().bind(I18N.localize("org.to2mbn.lolixl.ui.impl.component.view.auth.addnewprofile.textlabel.text"));
	}
}
