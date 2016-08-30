package org.to2mbn.lolixl.ui.component;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.DoubleProperty;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableDoubleProperty;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import java.util.List;
import java.util.Objects;
import org.to2mbn.lolixl.ui.DisplayableItem;

/**
 * 从 {@link DisplayableItem} 创建的磁贴。
 * <p>
 * 该磁贴看起来是这样子的：<br>
 * |[Image] [Text]|<br>
 * 如果说没有Image，那么就会这样：<br>
 * |[Text]|
 * 
 * @author yushijinhun
 */
public class ItemTile extends Tile {

	public static final String CSS_CLASS_ITEM_TILE = "xl-item-tile";

	private DoubleProperty iconWidthProperty = new SimpleStyleableDoubleProperty(StyleableProperties.ICON_WIDTH, this, "iconWidth", 0d);
	private DoubleProperty iconHeightProperty = new SimpleStyleableDoubleProperty(StyleableProperties.ICON_HEIGHT, this, "iconHeight", 0d);

	public ItemTile(DisplayableItem item) {
		Objects.requireNonNull(item);
		getStyleClass().add(CSS_CLASS_ITEM_TILE);

		textProperty().bind(item.getLocalizedName());

		ImageView iconView = new ImageView();
		iconView.imageProperty().bind(item.getIcon());
		iconView.fitWidthProperty().bind(iconWidthProperty);
		iconView.fitHeightProperty().bind(iconHeightProperty);
		graphicProperty().bind(new ObjectBinding<Node>() {

			{
				bind(iconView.imageProperty());
			}

			@Override
			protected Node computeValue() {
				if (iconView.imageProperty().get() == null) {
					return null;
				} else {
					return iconView;
				}
			}
		});
	}

	public DoubleProperty iconWidthProperty() {
		return iconWidthProperty;
	}

	public DoubleProperty iconHeightProperty() {
		return iconHeightProperty;
	}

	@SuppressWarnings("unchecked")
	private static final class StyleableProperties {

		static final StyleablePropertyFactory<ItemTile> FACTORY = new StyleablePropertyFactory<>(Tile.getClassCssMetaData());

		static final CssMetaData<ItemTile, Number> ICON_WIDTH = FACTORY.createSizeCssMetaData("-xl-icon-width", s -> (StyleableProperty<Number>) s.iconWidthProperty, 0d);
		static final CssMetaData<ItemTile, Number> ICON_HEIGHT = FACTORY.createSizeCssMetaData("-xl-icon-height", s -> (StyleableProperty<Number>) s.iconHeightProperty, 0d);
	}

	public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
		return StyleableProperties.FACTORY.getCssMetaData();
	}

	@Override
	public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
		return getClassCssMetaData();
	}
}
