package org.to2mbn.lolixl.ui.component;

import javafx.scene.control.Button;
import javafx.scene.effect.Effect;
import javafx.scene.effect.PerspectiveTransform;
import javafx.scene.input.MouseEvent;
import org.to2mbn.lolixl.utils.tile.TilePerspectiveUtils;
import java.util.logging.Logger;

public class Tile extends Button {
	public Tile() {
		//addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
		//addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);
		setDefaultButton(true);
	}

	private void onMousePressed(MouseEvent event) {
		PerspectiveTransform transform = TilePerspectiveUtils.compute(event.getSceneX() - getLayoutX(), event.getSceneY() - getLayoutY(), this);
		Logger.getLogger(getClass().getCanonicalName()).fine("Computed transform [" + transform + "]");
		Effect effect = transform;
		setEffect(effect);
	}

	private void onMouseReleased(MouseEvent event) {
		setEffect(null);
	}
}
