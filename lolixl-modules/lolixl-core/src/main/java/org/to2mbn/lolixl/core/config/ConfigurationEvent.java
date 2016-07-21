package org.to2mbn.lolixl.core.config;

import java.util.HashMap;
import java.util.Map;
import org.osgi.service.event.Event;

public class ConfigurationEvent extends Event {

	public static final String TOPIC_CONFIGURATION = "org/to2mbn/lolixl/core/config/changed";
	public static final String KEY_CATEGORY = "org.to2mbn.lolixl.core.config.category";
	public static final String KEY_CONFIG = "org.to2mbn.lolixl.core.config.config";
	public static final String KEY_TYPE = "org.to2mbn.lolixl.core.config.type";

	private Configuration configuration;
	private String category;

	private static Map<String, Object> createProperties(Configuration configuration, String category) {
		Map<String, Object> properties = new HashMap<>();
		properties.put(KEY_CONFIG, configuration);
		properties.put(KEY_CATEGORY, category);
		return properties;
	}

	public ConfigurationEvent(Configuration configuration, String category) {
		super(TOPIC_CONFIGURATION, createProperties(configuration, category));
		this.configuration = configuration;
		this.category = category;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public String getCategory() {
		return category;
	}
}
