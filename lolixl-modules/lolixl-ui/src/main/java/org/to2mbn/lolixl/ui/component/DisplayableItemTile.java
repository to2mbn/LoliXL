package org.to2mbn.lolixl.ui.component;

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

	private DisplayableItem item;

	public DisplayableItemTile(DisplayableItem item) {
		this.item = item;
	}

	// TODO

}