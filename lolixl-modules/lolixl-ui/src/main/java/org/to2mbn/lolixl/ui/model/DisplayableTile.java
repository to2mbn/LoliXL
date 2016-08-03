package org.to2mbn.lolixl.ui.model;

import org.to2mbn.lolixl.ui.component.DisplayableItemTile;
import org.to2mbn.lolixl.ui.component.Tile;

public interface DisplayableTile extends DisplayableItem {

	default Tile createTile() {
		return new DisplayableItemTile(this);
	}

}
