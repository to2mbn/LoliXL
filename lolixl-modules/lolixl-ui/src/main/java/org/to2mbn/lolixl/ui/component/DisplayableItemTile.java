package org.to2mbn.lolixl.ui.component;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import org.to2mbn.lolixl.ui.component.view.DisplayableItemTileView;
import org.to2mbn.lolixl.ui.model.DisplayableItem;

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
public class DisplayableItemTile extends Tile {

	private final DisplayableItem item;
	private final DisplayableItemTileView graphic;

	public DisplayableItemTile(DisplayableItem item) {
		this.item = item;
		setContentDisplay(ContentDisplay.TOP);

		graphic = new DisplayableItemTileView();
		graphic.textLabel.textProperty().bind(item.getLocalizedName());
		graphic.iconView.imageProperty().bind(new ObjectBinding<Image>() {
			ObservableObjectValue<Image> image = item.getIcon();

			{
				bind(image);
			}

			@Override
			protected Image computeValue() {
				return image.get() != null ? image.get() : new Image("/ui/img/no_icon.png");
			}
		});
		// for animation:
		prefWidthProperty().addListener(new WeakChangeListener<>(((observable, oldValue, newValue) -> {
			graphic.setPrefWidth(newValue.doubleValue());
			graphic.resize(newValue.doubleValue(), getPrefHeight());
		})));
		prefHeightProperty().addListener(new WeakChangeListener<>(((observable, oldValue, newValue) -> {
			graphic.setPrefHeight(newValue.doubleValue());
			graphic.resize(getPrefWidth(), newValue.doubleValue());
		})));
		setGraphic(graphic);
	}
}
