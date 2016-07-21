package org.to2mbn.lolixl.core.config;

import java.util.HashMap;
import java.util.Map;
import org.osgi.service.event.Event;

public class ConfigurationEvent extends Event {

	/**
	 * 配置运行时发生了改变，全局广播更新事件。
	 */
	public static final int TYPE_UPDATE = 1;

	/**
	 * 当一个新的EventListener被注册的时候，如果该listener监听的配置已注册， 那么框架将会单独把这个配置发给listener。
	 */
	public static final int TYPE_NOTIFY = 2;

	public static final String TOPIC_CONFIGURATION = "org/to2mbn/lolixl/core/config/changed";
	public static final String KEY_CATEGORY = "org.to2mbn.lolixl.core.config.category";
	public static final String KEY_CONFIG = "org.to2mbn.lolixl.core.config.config";
	public static final String KEY_TYPE = "org.to2mbn.lolixl.core.config.type";

	private Configuration configuration;
	private int type;
	private String category;

	private static Map<String, Object> createProperties(Configuration configuration, int type, String category) {
		Map<String, Object> properties = new HashMap<>();
		properties.put(KEY_CONFIG, configuration);
		properties.put(KEY_TYPE, type);
		properties.put(KEY_CATEGORY, category);
		return properties;
	}

	public ConfigurationEvent(Configuration configuration, int type, String category) {
		super(TOPIC_CONFIGURATION, createProperties(configuration, type, category));
		this.configuration = configuration;
		this.type = type;
		this.category = category;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public int getType() {
		return type;
	}

	public String getCategory() {
		return category;
	}
}
