package org.to2mbn.lolixl.ui.component;

import javafx.scene.control.Button;
import javafx.scene.effect.Effect;
import javafx.scene.input.MouseEvent;
import org.to2mbn.lolixl.utils.tile.TilePerspectiveUtils;

public class Tile extends Button {
	public Tile() {
		addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
		addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);
	}

	private void onMousePressed(MouseEvent event) {
		Effect effect = TilePerspectiveUtils.compute(event.getSceneX() - getLayoutX(), event.getSceneY() - getLayoutY(), this);
		setEffect(effect);
	}

	private void onMouseReleased(MouseEvent event) {
		setEffect(null);
	}
}
