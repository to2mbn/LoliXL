package org.to2mbn.lolixl.api.event;

import java.util.EventObject;
import org.to2mbn.lolixl.api.annotation.Plugin;

abstract public class PluginEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	private Plugin pluginDescription;

	public PluginEvent(Object source, Plugin pluginDescription) {
		super(source);
		this.pluginDescription = pluginDescription;
	}

	public Plugin getPluginDescription() {
		return pluginDescription;
	}

}
