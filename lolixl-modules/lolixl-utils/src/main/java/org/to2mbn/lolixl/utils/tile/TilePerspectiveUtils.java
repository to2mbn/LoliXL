package org.to2mbn.lolixl.utils.tile;

import java.util.function.Function;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.geometry.Pos;
import javafx.scene.effect.PerspectiveTransform;

public class TilePerspectiveUtils {

	private static class Perspective {

		static final double SHALLOW = 2D;
		static final double DEEP = 4D;

		double ulx, uly, urx, ury, lrx, lry, llx, lly;

		Perspective(double x, double y, double width, double height, Pos noEffectPos) {
			double ratioX = x / width / 3;
			double ratioY = y / height / 3;
			if (ratioY <= 1D) {
				if (ratioX <= 1D) {
					ulx = DEEP;
					uly = DEEP;
					urx = width - SHALLOW;
					ury = SHALLOW;
					lrx = width - SHALLOW;
					lry = height - SHALLOW;
					llx = SHALLOW;
					lly = height - SHALLOW;
				} else if (ratioX > 1D && ratioX <= 2D) {
					ulx = DEEP;
					uly = DEEP;
					urx = width - DEEP;
					ury = DEEP;
					lrx = width - SHALLOW;
					lry = height - SHALLOW;
					llx = SHALLOW;
					lly = height - SHALLOW;
				} else {
					ulx = SHALLOW;
					uly = SHALLOW;
					urx = width - DEEP;
					ury = DEEP;
					lrx = width - SHALLOW;
					lry = height - SHALLOW;
					llx = SHALLOW;
					lly = height - SHALLOW;
				}
			} else if (ratioY > 1D && ratioY <= 2D) {
				if (ratioX <= 1D) {
					ulx = DEEP;
					uly = DEEP;
					urx = width - SHALLOW;
					ury = SHALLOW;
					lrx = width - SHALLOW;
					lry = height - SHALLOW;
					llx = DEEP;
					lly = height - DEEP;
				} else if (ratioX > 1D && ratioX <= 2D) {
					ulx = DEEP;
					uly = DEEP;
					urx = width - DEEP;
					ury = DEEP;
					lrx = width - DEEP;
					lry = height - DEEP;
					llx = DEEP;
					lly = height - DEEP;
				} else {
					ulx = SHALLOW;
					uly = SHALLOW;
					urx = width - DEEP;
					ury = DEEP;
					lrx = width - DEEP;
					lry = height - DEEP;
					llx = SHALLOW;
					lly = height - SHALLOW;
				}
			} else if (ratioY > 2D) {
				if (ratioX <= 1D) {
					ulx = SHALLOW;
					uly = SHALLOW;
					urx = width - SHALLOW;
					ury = SHALLOW;
					lrx = width - SHALLOW;
					lry = height - SHALLOW;
					llx = DEEP;
					lly = height - DEEP;
				} else if (ratioX > 1D && ratioX <= 2D) {
					ulx = SHALLOW;
					uly = SHALLOW;
					urx = width - SHALLOW;
					ury = SHALLOW;
					lrx = width - DEEP;
					lry = height - DEEP;
					llx = DEEP;
					lly = height - DEEP;
				} else {
					ulx = SHALLOW;
					uly = SHALLOW;
					urx = width - SHALLOW;
					ury = SHALLOW;
					lrx = width - DEEP;
					lry = height - DEEP;
					llx = SHALLOW;
					lly = height - SHALLOW;
				}
			} else {
				throw new IllegalArgumentException("Illegal width/height");
			}

			switch (noEffectPos) {
				case BOTTOM_CENTER:
					llx = 0D;
					lly = height;
					lrx = width;
					lry = height;
					break;
				case BOTTOM_LEFT:
					llx = 0D;
					lly = height;
					break;
				case BOTTOM_RIGHT:
					lrx = width;
					lry = height;
					break;
				case CENTER_LEFT:
					ulx = 0D;
					uly = 0D;
					llx = 0D;
					lly = height;
					break;
				case CENTER_RIGHT:
					urx = width;
					ury = 0D;
					lrx = width;
					lry = height;
					break;
				case TOP_CENTER:
					ulx = 0D;
					uly = 0D;
					urx = width;
					ury = 0D;
					break;
				case TOP_LEFT:
					ulx = 0D;
					uly = 0D;
					break;
				case TOP_RIGHT:
					urx = width;
					ury = 0D;
					break;
				default:
					break;
			}
		}

	}

	public static PerspectiveTransform computeEnd(ObservableDoubleValue x, ObservableDoubleValue y, ObservableDoubleValue width, ObservableDoubleValue height, ObservableObjectValue<Pos> noEffectPos) {
		ObjectBinding<Perspective> perspective = Bindings.createObjectBinding(() -> new Perspective(x.get(), y.get(), width.get(), height.get(), noEffectPos.get()), x, y, width, height, noEffectPos);
		PerspectiveTransform effect = new PerspectiveTransform();
		effect.ulxProperty().bind(createPerspectivePropertyBinding(p -> p.ulx, perspective));
		effect.ulyProperty().bind(createPerspectivePropertyBinding(p -> p.uly, perspective));
		effect.urxProperty().bind(createPerspectivePropertyBinding(p -> p.urx, perspective));
		effect.uryProperty().bind(createPerspectivePropertyBinding(p -> p.ury, perspective));
		effect.lrxProperty().bind(createPerspectivePropertyBinding(p -> p.lrx, perspective));
		effect.lryProperty().bind(createPerspectivePropertyBinding(p -> p.lry, perspective));
		effect.llxProperty().bind(createPerspectivePropertyBinding(p -> p.llx, perspective));
		effect.llyProperty().bind(createPerspectivePropertyBinding(p -> p.lly, perspective));
		return effect;

	}

	private static DoubleBinding createPerspectivePropertyBinding(Function<Perspective, Double> func, ObservableObjectValue<Perspective> perspective) {
		return Bindings.createDoubleBinding(() -> func.apply(perspective.get()), perspective);
	}

}
