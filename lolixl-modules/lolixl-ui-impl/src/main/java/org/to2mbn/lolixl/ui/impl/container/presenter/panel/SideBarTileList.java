package org.to2mbn.lolixl.ui.impl.container.presenter.panel;

import java.io.Serializable;
import java.util.Map;
import java.util.Vector;
import org.to2mbn.lolixl.core.config.Configuration;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.model.SidebarTileElement;

public class SideBarTileList implements Configuration {

	public static class TileEntry implements Serializable {

		private static final long serialVersionUID = 1L;

		public String tagName;
		public volatile SidebarTileElement tileElement;
		public volatile Tile tileComponent;

		@Override
		public String toString() {
			return "[" + tagName + "]";
		}

	}

	private static final long serialVersionUID = 1L;

	public Vector<TileEntry> entries;
	public volatile Map<String, TileEntry> tagNameMapping;
	public volatile Map<SidebarTileElement, TileEntry> serviceMapping;
	public volatile Map<Tile, TileEntry> componentMapping;

	@Override
	public String toString() {
		return String.valueOf(entries);
	}

}