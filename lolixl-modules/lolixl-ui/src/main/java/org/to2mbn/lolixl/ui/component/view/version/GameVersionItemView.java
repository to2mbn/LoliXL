package org.to2mbn.lolixl.ui.component.view.version;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.to2mbn.lolixl.ui.component.VersionTag;
import org.to2mbn.lolixl.utils.BundleUtils;
import org.to2mbn.lolixl.utils.FXUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

public class GameVersionItemView extends BorderPane {
	private static final String FXML_LOCATION = "/ui/fxml/component/game_version_group_item.fxml";

	@FXML
	public ImageView iconView;

	@FXML
	public BorderPane infoContainer;

	@FXML
	public Label versionNameLabel;

	@FXML
	public HBox versionTagContainer;

	public GameVersionItemView() {
		FXMLLoader loader = new FXMLLoader(BundleUtils.getResourceFromBundle(getClass(), FXML_LOCATION));
		loader.setRoot(this);
		loader.setController(this);
		try {
			loader.load();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		// TODO: default icon
	}

	public void addTag(VersionTag tag) {
		Objects.requireNonNull(tag);
		FXUtils.checkFxThread();
		ImageView component = new ImageView();
		component.setId(tag.getCssId());
		versionTagContainer.getChildren().add(component);
	}

	public void removeTag(VersionTag tag) {
		Objects.requireNonNull(tag);
		FXUtils.checkFxThread();
		for (Node child : versionTagContainer.getChildren()) {
			if (child.getId().equals(tag.getCssId())) {
				versionTagContainer.getChildren().remove(child);
			}
		}
	}
}
