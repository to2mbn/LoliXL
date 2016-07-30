package org.to2mbn.lolixl.utils.tile;

import javafx.scene.control.Button;
import javafx.scene.effect.PerspectiveTransform;

public class TilePerspectiveUtils {
	public static PerspectiveTransform compute(double x, double y, Button tile) {
		double width = tile.getWidth();
		double height = tile.getHeight();
		double ratioX = x / width / 3;
		double ratioY = y / height / 3;
		if (ratioY <= 1D) {
			if (ratioX <= 1D) {
				return new PerspectiveTransform(4D, 4D, width - 2D, 2D, width - 2D, height - 2D, 2D, height - 2D);
			} else if (ratioX > 1D && ratioX <= 2D) {
				return new PerspectiveTransform(4D, 4D, width - 4D, 4D, width - 2D, height - 2D, 2D, height - 2D);
			} else if (ratioX > 2D) {
				return new PerspectiveTransform(2D, 2D, width - 4D, 4D, width - 2D, height - 2D, 2D, height - 2D);
			}
		} else if (ratioY > 1D && ratioY <= 2D) {
			if (ratioX <= 1D) {
				return new PerspectiveTransform(4D, 4D, width - 2D, 2D, width - 2D, height - 2D, 4D, height - 4D);
			} else if (ratioX > 1D && ratioX <= 2D) {
				return new PerspectiveTransform(4D, 4D, width - 4D, 4D, width - 4D, height - 4D, 4D, height - 4D);
			} else if (ratioX > 2D) {
				return new PerspectiveTransform(2D, 2D, width - 4D, 4D, width - 4D, height - 4D, 2D, height - 2D);
			}
		} else if (ratioY > 2D) {
			if (ratioX <= 1D) {
				return new PerspectiveTransform(2D, 2D, width - 2D, 2D, width - 2D, height - 2D, 4D, height - 4D);
			} else if (ratioX > 1D && ratioX <= 2D) {
				return new PerspectiveTransform(2D, 2D, width - 2D, 2D, width - 4D, height - 4D, 4D, height - 4D);
			} else if (ratioX > 2D) {
				return new PerspectiveTransform(2D, 2D, width - 2D, 2D, width - 4D, height - 4D, 2D, height - 2D);
			}
		}
		throw new Error(); // impossible
	}
}
