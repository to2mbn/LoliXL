package org.to2mbn.lolixl.api.event;

import org.to2mbn.lolixl.api.annotation.Plugin;

public class PluginEnableEvent extends PluginEvent {

	private static final long serialVersionUID = 1L;

	public PluginEnableEvent(Object source, Plugin pluginDescription) {
		super(source, pluginDescription);
	}

}
