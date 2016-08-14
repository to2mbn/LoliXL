package org.to2mbn.lolixl.ui.component.view.auth;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.utils.BundleUtils;
import java.io.IOException;
import java.io.UncheckedIOException;

// TODO: 移动至offline auth模块中
public class OfflineAuthProfileView extends BorderPane {
	private static final String FXML_LOCATION = "/ui/fxml/component/offline_auth_profile_panel.fxml";

	@FXML
	public BorderPane bottomContainer;

	@FXML
	public GridPane contentContainer;

	@FXML
	public BorderPane avatarPathContainer;

	@FXML
	public Button finishButton;

	@FXML
	public Label userNameLabel;

	@FXML
	public Label avatarPathLabel;

	@FXML
	public TextField userNameInput;

	@FXML
	public TextField avatarPathInput;

	@FXML
	public Button chooseAvatarButton;

	@FXML
	public Label avatarLabel;

	@FXML
	public ImageView avatarView;

	public OfflineAuthProfileView() {
		FXMLLoader loader = new FXMLLoader(BundleUtils.getResourceFromBundle(getClass(), FXML_LOCATION));
		loader.setRoot(this);
		loader.setController(this);
		try {
			loader.load();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		userNameLabel.textProperty().bind(I18N.localize("org.to2mbn.lolixl.ui.impl.component.view.auth.label.username.text"));
		avatarPathLabel.textProperty().bind(I18N.localize("org.to2mbn.lolixl.ui.impl.component.view.auth.label.avatarpath.text"));
		avatarLabel.textProperty().bind(I18N.localize("org.to2mbn.lolixl.ui.impl.component.view.auth.label.avatar.text"));
		finishButton.textProperty().bind(I18N.localize("org.to2mbn.lolixl.ui.impl.component.view.auth.button.finish.text"));
		chooseAvatarButton.textProperty().bind(I18N.localize("org.to2mbn.lolixl.ui.impl.component.view.auth.button.choose.text"));
		// TODO: default steve avatar
	}
}
