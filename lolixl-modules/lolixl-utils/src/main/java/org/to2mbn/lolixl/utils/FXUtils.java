package org.to2mbn.lolixl.utils;

import javafx.application.Platform;
import javafx.css.Styleable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;

public final class FXUtils {

	private FXUtils() {}

	public static void checkFxThread() {
		if (!Platform.isFxApplicationThread())
			throw new IllegalStateException("Not on FX application thread; currentThread = " + Thread.currentThread().getName());
	}

	public static void setButtonGraphic(Button button, Node graphic) {
		button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		button.setPadding(Insets.EMPTY);
		button.setGraphic(graphic);
	}

	public static void setCssClass(Styleable component, String... clazz) {
		component.getStyleClass().setAll(clazz);
	}

}
