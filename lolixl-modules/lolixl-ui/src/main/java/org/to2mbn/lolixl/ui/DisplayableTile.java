package org.to2mbn.lolixl.ui;

import org.to2mbn.lolixl.ui.component.ItemTile;
import org.to2mbn.lolixl.ui.component.Tile;

public interface DisplayableTile extends DisplayableItem {

	default Tile createTile() {
		return new ItemTile(this);
	}

}
