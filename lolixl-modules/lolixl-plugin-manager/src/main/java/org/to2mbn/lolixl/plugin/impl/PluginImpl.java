package org.to2mbn.lolixl.plugin.impl;

import java.util.concurrent.CompletableFuture;
import org.osgi.framework.Bundle;
import org.to2mbn.lolixl.plugin.Plugin;
import org.to2mbn.lolixl.plugin.PluginDescription;

public class PluginImpl implements Plugin, Comparable<Plugin> {

	PluginServiceImpl container;
	PluginDescription description;
	Bundle bundle;

	@Override
	public PluginDescription getDescription() {
		return description;
	}

	@Override
	public Bundle getBundle() {
		return bundle;
	}

	@Override
	public CompletableFuture<Void> unload() {
		return container.unloadPlugin(this);
	}

	@Override
	public int compareTo(Plugin o) {
		return description.getArtifact().compareTo(o.getDescription().getArtifact());
	}

}
