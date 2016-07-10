package org.to2mbn.lolixl.plugin.impl;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Bundle;
import org.to2mbn.lolixl.plugin.Plugin;
import org.to2mbn.lolixl.plugin.PluginService;
import org.to2mbn.lolixl.plugin.maven.LocalMavenRepository;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;

@Service({ PluginService.class })
@Component
public class PluginServiceImpl implements PluginService {

	@Override
	public Set<Plugin> getLoadedPlugins() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Plugin> getPlugin(MavenArtifact artifact) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Plugin> getPlugin(Bundle bundle) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Plugin> loadPlugin(LocalMavenRepository repository, MavenArtifact artifact) {
		// TODO Auto-generated method stub
		return null;
	}

}
