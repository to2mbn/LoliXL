package org.to2mbn.lolixl.ui.impl.container.presenter.panel;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import org.to2mbn.lolixl.core.config.Configuration;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.model.SidebarTileElement;

public class SideBarTileList implements Configuration {

	public static class TileEntry implements Serializable {

		private static final long serialVersionUID = 1L;

		public String tagName;
		public volatile transient SidebarTileElement tileElement;
		public volatile transient Tile tileComponent;

	}

	private static final long serialVersionUID = 1L;

	public List<TileEntry> entries = new Vector<>();
	public transient Map<String, TileEntry> tagNameMapping = new ConcurrentHashMap<>();
	public transient Map<SidebarTileElement, TileEntry> serviceMapping = new ConcurrentHashMap<>();
	public transient Map<Tile, TileEntry> componentMapping = new ConcurrentHashMap<>();

}