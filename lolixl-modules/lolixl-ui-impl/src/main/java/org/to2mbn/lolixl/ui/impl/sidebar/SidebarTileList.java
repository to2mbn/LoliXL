package org.to2mbn.lolixl.ui.impl.sidebar;

import org.to2mbn.lolixl.core.config.Configuration;
import org.to2mbn.lolixl.ui.sidebar.SidebarTileElement;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class SidebarTileList implements Configuration {

	public static class TileEntry implements Serializable {

		private static final long serialVersionUID = 1L;

		public String tagName;
		public volatile transient SidebarTileElement tileElement;

	}

	private static final long serialVersionUID = 1L;

	public List<TileEntry> entries = new Vector<>();
	public transient Map<String, TileEntry> tagNameMapping = new ConcurrentHashMap<>();
	public transient Map<SidebarTileElement, TileEntry> serviceMapping = new ConcurrentHashMap<>();

}