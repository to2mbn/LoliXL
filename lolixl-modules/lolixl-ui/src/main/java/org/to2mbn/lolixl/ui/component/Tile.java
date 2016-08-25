package org.to2mbn.lolixl.ui.component;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
import org.to2mbn.lolixl.utils.binding.FxConstants;

/**
 * 代表一个磁贴。
 * 
 * @author yushijinhun
 */
public class Tile extends Button {

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
