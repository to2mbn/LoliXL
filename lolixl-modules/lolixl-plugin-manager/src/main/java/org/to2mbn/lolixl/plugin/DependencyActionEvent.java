package org.to2mbn.lolixl.plugin;

import java.util.HashMap;
import java.util.Map;
import org.osgi.service.event.Event;

public class DependencyActionEvent extends Event {

	public static final String TOPIC_DEPENDENCY_ACTION = "org.to2mbn.lolixl.plugin.dependencyAction";
	public static final String KEY_DEPENDENCY_ACTION = "org.to2mbn.lolixl.plugin.action";

	private static Map<String, Object> createProperties(DependencyAction action) {
		Map<String, Object> properties = new HashMap<>();
		properties.put(KEY_DEPENDENCY_ACTION, action);
		return properties;
	}

	private DependencyAction action;

	public DependencyActionEvent(DependencyAction action) {
		super(TOPIC_DEPENDENCY_ACTION, createProperties(action));
		this.action = action;
	}

	public DependencyAction getAction() {
		return action;
	}

}
