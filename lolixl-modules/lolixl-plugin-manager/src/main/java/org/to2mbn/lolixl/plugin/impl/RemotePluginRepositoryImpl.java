package org.to2mbn.lolixl.plugin.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.lolixl.plugin.PluginRepository;
import org.to2mbn.lolixl.plugin.maven.MavenRepository;

@Service({ PluginRepository.class })
@Component
@Properties({
		@Property(name = "pluginRepo.type", value = "remote")
})
public class RemotePluginRepositoryImpl extends AbstractPluginRepository implements PluginRepository {

	@Reference(target = "(m2repository.type=remote)")
	private MavenRepository remoteM2Repo;

	@Override
	public MavenRepository getRepository() {
		return remoteM2Repo;
	}

}
