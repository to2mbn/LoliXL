package org.to2mbn.lolixl.ui.component;

import javafx.beans.property.BooleanProperty;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableBooleanProperty;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.effect.PerspectiveTransform;
import javafx.scene.input.MouseEvent;
import java.util.List;
import org.to2mbn.lolixl.utils.tile.TilePerspectiveUtils;

/**
 * 代表一个磁贴。
 * 
 * @author yushijinhun
 */
public class Tile extends Button {

	public static final String CSS_CLASS_TILE = "xl-tile";

	/**
	 * 如果要给一个Tile开启按下特效，则需要：
	 * 
	 * <pre>
	 * <code>
	 * tile.getStyleClass().add(CSS_CLASS_EFFECT_TILE);
	 * </code>
	 * </pre>
	 * 
	 * 若要关闭则remove即可。该特效默认关闭。
	 */
	public static final String CSS_CLASS_EFFECT_TILE = "xl-effect-tile";

	private BooleanProperty showTileEffectProperty = new SimpleStyleableBooleanProperty(StyleableProperties.SHOW_TILE_EFFECT, this, "showTileEffect", false);

	public Tile() {
		addEventHandler(MouseEvent.MOUSE_PRESSED, this::showEffect);
		addEventHandler(MouseEvent.MOUSE_RELEASED, e -> hideEffect());
		addEventHandler(MouseEvent.MOUSE_EXITED, e -> hideEffect());
		addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
			if (isPressed())
				showEffect(e);
		});

		getStyleClass().add(CSS_CLASS_TILE);
	}

	private void showEffect(MouseEvent event) {
		if (showTileEffectProperty.get()) {
			PerspectiveTransform transform = TilePerspectiveUtils.compute(event.getSceneX() - getLayoutX(), event.getSceneY() - getLayoutY(), this);
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
	}

	public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
		return StyleableProperties.FACTORY.getCssMetaData();
	}

	@Override
	public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
		return getClassCssMetaData();
	}

}
