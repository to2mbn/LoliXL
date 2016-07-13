package org.to2mbn.lolixl.init;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.plugin.LocalPluginRepository;
import org.to2mbn.lolixl.plugin.PluginRepository;
import org.to2mbn.lolixl.plugin.PluginService;

@Component
public class FelixInit {

	@Reference
	private PluginService pluginService;

	@Reference(target = "(pluginRepo.type=local)")
	private LocalPluginRepository localPluginRepo;

	@Reference(target = "(pluginRepo.type=remote)")
	private PluginRepository remotePluginRepo;

	@Activate
	public void active(ComponentContext compCtx) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/org.to2mbn.lolixl.init.plugins.list"), "UTF-8"))) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#"))
					continue;
				String[] splited = line.split(":", 2);
				String groupId = splited[0];
				String artifactId = splited[1];
				// TODO
			}
		}

	}

}
