package org.to2mbn.lolixl.init;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.plugin.LocalPluginRepository;
import org.to2mbn.lolixl.plugin.PluginService;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;
import org.to2mbn.lolixl.plugin.maven.MavenRepository;

@Component
public class FelixInit {

	@Reference
	private PluginService pluginService;

	@Reference(target = "(pluginRepo.type=local)")
	private LocalPluginRepository localPluginRepo;

	@Reference(target = "(m2repository.type=remote)")
	private MavenRepository remoteM2Repo;

	@Activate
	public void active(ComponentContext compCtx) {
		// TODO
	}

}
