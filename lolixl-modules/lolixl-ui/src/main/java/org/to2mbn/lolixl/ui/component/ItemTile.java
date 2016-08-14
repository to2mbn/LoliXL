package org.to2mbn.lolixl.ui.component;

import javafx.beans.Observable;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.to2mbn.lolixl.ui.component.view.DisplayableItemTileView;
import org.to2mbn.lolixl.ui.model.DisplayableItem;
import org.to2mbn.lolixl.utils.FXUtils;

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

	public static final String CSS_CLASS_ITEM_TILE = "xl-tiem-tile";

	private DisplayableItem item;

	public ItemTile(DisplayableItem item) {
		this.item = item;

		getStyleClass().add(CSS_CLASS_ITEM_TILE);

		textProperty().bind(item.getLocalizedName());

		ImageView iconView = new ImageView();
		iconView.imageProperty().bind(item.getIcon());
		graphicProperty().set(iconView);
	}
}
