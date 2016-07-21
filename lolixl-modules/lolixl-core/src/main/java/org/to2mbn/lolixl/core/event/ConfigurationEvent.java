package org.to2mbn.lolixl.core.event;

import java.util.HashMap;
import java.util.Map;
import org.osgi.service.event.Event;
import org.to2mbn.lolixl.core.config.Configuration;

public class ConfigurationEvent extends Event {

	/**
	 * 一个EventListener的属性的key，该property的值value监听的配置类的全限定名。
	 * 如果一个EventListener拥有该property，框架只会通知配置是value中指定的类（的子类）的更变。
	 */
	public static final String LISTENER_PROPERTY_CONFIG_TYPE = "org.to2mbn.lolixl.core.event.configuration.config.type";

	public static enum ConfigurationEventType {

		/**
		 * 配置运行时发生了改变，全局广播更新事件。
		 */
		UPDATE,

		/**
		 * 当一个新的EventListener被注册的时候，如果该listener监听的配置已注册，
		 * 那么框架将会单独把这个配置发给listener。
		 */
		NOTIFY;
	}

	public static final String TOPIC_DOWNLOAD_START = "org/to2mbn/lolixl/core/event/configuration";
	public static final String KEY_CONFIG = "org.to2mbn.lolixl.core.event.configuration.config";
	public static final String KEY_TYPE = "org.to2mbn.lolixl.core.event.configuration.type";

	private Configuration configuration;
	private ConfigurationEventType type;

	private static Map<String, Object> createProperties(Configuration configuration, ConfigurationEventType type) {
		Map<String, Object> properties = new HashMap<>();
		properties.put(KEY_CONFIG, configuration);
		properties.put(KEY_TYPE, type);
		return properties;
	}

	public ConfigurationEvent(Configuration configuration, ConfigurationEventType type) {
		super(TOPIC_DOWNLOAD_START, createProperties(configuration, type));
		this.configuration = configuration;
		this.type = type;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public ConfigurationEventType getType() {
		return type;
	}
}
