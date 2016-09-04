package org.to2mbn.lolixl.ui.event;

import java.util.HashMap;
import java.util.Map;
import org.osgi.service.event.Event;
import org.to2mbn.lolixl.plugin.Plugin;

public class CssApplyEvent extends Event {

	public static final String TOPIC_CSS_APPLY = "org/to2mbn/lolixl/ui/cssApply";
	public static final String KEY_PLUGIN = "org.to2mbn.lolixl.ui.cssApply.plugin";

	private static Map<String, Object> createProperties(Plugin plugin) {
		Map<String, Object> properties = new HashMap<>();
		properties.put(KEY_PLUGIN, plugin);
		return properties;
	}

	private Plugin plugin;

	public CssApplyEvent(Plugin plugin) {
		super(TOPIC_CSS_APPLY, createProperties(plugin));
		this.plugin = plugin;
	}

	public Plugin getPlugin() {
		return plugin;
	}

}
