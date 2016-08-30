package org.to2mbn.lolixl.utils;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.Region;

public final class FXUtils {

	private FXUtils() {}

	public static void checkFxThread() {
		if (!Platform.isFxApplicationThread())
			throw new IllegalStateException("Not on FX application thread; currentThread = " + Thread.currentThread().getName());
	}

	public static boolean isFxInitialized() {
		try {
			Platform.runLater(() -> {});
		} catch (IllegalStateException e) {
			return false;
		}
		return true;
	}

	public static void setSizeToPref(Region node) {
		node.maxWidthProperty().set(Region.USE_PREF_SIZE);
		node.minWidthProperty().set(Region.USE_PREF_SIZE);
		node.maxHeightProperty().set(Region.USE_PREF_SIZE);
		node.minHeightProperty().set(Region.USE_PREF_SIZE);
	}

	public static void bindPrefSize(Region a, Region b) {
		a.prefWidthProperty().bind(b.widthProperty());
		a.prefHeightProperty().bind(b.heightProperty());
	}

	// TODO: 删除此方法
	@Deprecated
	public static void setButtonGraphic(Button button, Node graphic) {
		button.setGraphic(graphic);
		button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		button.setPadding(Insets.EMPTY);
	}

}
