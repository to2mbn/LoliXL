package org.to2mbn.lolixl.ui.component;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableValue;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableBooleanProperty;
import javafx.css.SimpleStyleableObjectProperty;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.effect.PerspectiveTransform;
import javafx.scene.input.MouseEvent;
import java.util.List;
import java.util.function.Function;
import org.to2mbn.lolixl.utils.binding.FxConstants;

/**
 * 代表一个磁贴。
 * 
 * @author yushijinhun
 */
public class Tile extends Button {

	static class TilePerspectiveUtils {

		static class Perspective {

			static final double SHALLOW = 1D;
			static final double DEEP = 2D;

			double ulx, uly, urx, ury, lrx, lry, llx, lly;

			Perspective(double ratioX, double ratioY, double width, double height, Pos noEffectPos) {
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
					// illegal width/height
					// fallback
					llx = 0D;
					lly = height;
					lrx = width;
					lry = height;
					ulx = 0D;
					uly = 0D;
					urx = width;
					ury = 0D;
				}

				if (noEffectPos != null) {
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

		}

		static PerspectiveTransform computeEnd(ObservableValue<? extends Number> x, ObservableValue<? extends Number> y, ObservableValue<? extends Number> width, ObservableValue<? extends Number> height, ObservableValue<Pos> noEffectPos) {
			double ratioX = x.getValue().doubleValue() / width.getValue().doubleValue() * 3;
			double ratioY = y.getValue().doubleValue() / height.getValue().doubleValue() * 3;
			ObjectBinding<Perspective> perspective = Bindings.createObjectBinding(() -> new Perspective(ratioX, ratioY, width.getValue().doubleValue(), height.getValue().doubleValue(), noEffectPos.getValue()), width, height, noEffectPos);
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

		static DoubleBinding createPerspectivePropertyBinding(Function<Perspective, Double> func, ObservableObjectValue<Perspective> perspective) {
			return Bindings.createDoubleBinding(() -> func.apply(perspective.get()), perspective);
		}
	}

	public static final String CSS_CLASS_TILE = "xl-tile";

	private BooleanProperty showTileEffectProperty = new SimpleStyleableBooleanProperty(StyleableProperties.SHOW_TILE_EFFECT, this, "showTileEffect", false);
	private ObjectProperty<Pos> noEffectPosProperty = new SimpleStyleableObjectProperty<Pos>(StyleableProperties.NO_EFFECT_POS, this, "noEffectPos", null);

	private DoubleProperty mouseX = new SimpleDoubleProperty();
	private DoubleProperty mouseY = new SimpleDoubleProperty();

	public Tile() {
		addEventHandler(MouseEvent.ANY, e -> {
			mouseX.set(e.getX());
			mouseY.set(e.getY());
		});
		addEventHandler(MouseEvent.MOUSE_PRESSED, e -> showEffect());
		addEventHandler(MouseEvent.MOUSE_RELEASED, e -> hideEffect());
		addEventHandler(MouseEvent.MOUSE_EXITED, e -> hideEffect());
		addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
			if (isPressed())
				showEffect();
		});

		getStyleClass().add(CSS_CLASS_TILE);
		getStyleClass().remove("button");
	}

	private void showEffect() {
		if (showTileEffectProperty.get()) {
			ObservableValue<Double> xConstant = FxConstants.object(mouseX.get());
			ObservableValue<Double> yConstant = FxConstants.object(mouseY.get());
			PerspectiveTransform transform = TilePerspectiveUtils.computeEnd(xConstant, yConstant, widthProperty(), heightProperty(), noEffectPosProperty);
			setEffect(transform);
		}
	}

	private void hideEffect() {
		if (showTileEffectProperty.get()) {
			setEffect(null);
		}
	}

	@SuppressWarnings("unchecked")
	private static final class StyleableProperties {

		static final StyleablePropertyFactory<Tile> FACTORY = new StyleablePropertyFactory<>(Labeled.getClassCssMetaData());

		static final CssMetaData<Tile, Boolean> SHOW_TILE_EFFECT = FACTORY.createBooleanCssMetaData("-xl-show-tile-effect", s -> (StyleableProperty<Boolean>) s.showTileEffectProperty, false);
		static final CssMetaData<Tile, Pos> NO_EFFECT_POS = FACTORY.createEnumCssMetaData(Pos.class, "-xl-no-effect-pos", s -> (StyleableObjectProperty<Pos>) s.noEffectPosProperty, null);
	}

	public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
		return StyleableProperties.FACTORY.getCssMetaData();
	}

	@Override
	public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
		return getClassCssMetaData();
	}

}
