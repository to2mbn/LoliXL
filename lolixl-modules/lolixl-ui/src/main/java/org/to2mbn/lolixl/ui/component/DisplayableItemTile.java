package org.to2mbn.lolixl.ui.component;

import org.to2mbn.lolixl.ui.component.view.DisplayableItemTileView;
import org.to2mbn.lolixl.ui.model.DisplayableItem;
import java.io.IOException;
import java.io.UncheckedIOException;

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

	private DisplayableItem item;
	private DisplayableItemTileView graphic;

	public DisplayableItemTile(DisplayableItem item) {
		this.item = item;
		try {
			graphic = new DisplayableItemTileView();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		setGraphic(graphic);

		graphic.textLabel.textProperty().bind(item.getLocalizedName());
		graphic.iconView.imageProperty().bind(item.getIcon());
	}

	// TODO

}
