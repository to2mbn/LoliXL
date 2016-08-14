package org.to2mbn.lolixl.ui.component;

import javafx.scene.control.Button;
import javafx.scene.effect.PerspectiveTransform;
import javafx.scene.input.MouseEvent;
import org.to2mbn.lolixl.utils.tile.TilePerspectiveUtils;
import java.util.logging.Logger;

/**
 * 代表一个磁贴。
 * 
 * @author yushijinhun
 */
public class Tile extends Button {

	public static final String CSS_CLASS_TILE = "xl-tile";

	public Tile() {
		//addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
		//addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);
		getStyleClass().add(CSS_CLASS_TILE);
	}

	private void onMousePressed(MouseEvent event) {
		PerspectiveTransform transform = TilePerspectiveUtils.compute(event.getSceneX() - getLayoutX(), event.getSceneY() - getLayoutY(), this);
		Logger.getLogger(getClass().getCanonicalName()).fine("Computed transform [" + transform + "]");
		setEffect(transform);
	}

	private void onMouseReleased(MouseEvent event) {
		setEffect(null);
	}
}
