package org.to2mbn.lolixl.api.event;

import org.to2mbn.lolixl.api.annotation.Plugin;

public class PluginDisableEvent extends PluginEvent {

	private static final long serialVersionUID = 1L;

	public PluginDisableEvent(Object source, Plugin pluginDescription) {
		super(source, pluginDescription);
	}

}
