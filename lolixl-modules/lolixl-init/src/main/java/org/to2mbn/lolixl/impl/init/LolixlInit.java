package org.to2mbn.lolixl.impl.init;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.to2mbn.lolixl.plugin.Plugin;
import org.to2mbn.lolixl.plugin.PluginManager;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;

@Component
public class LolixlInit {

	private static final Logger LOGGER = Logger.getLogger(LolixlInit.class.getCanonicalName());

	@Reference
	private PluginManager pluginManager;

	@Activate
	public void active() {
		new Thread(this::init, "LoliXL-startup").start();
	}

	private boolean shouldOverwriteSystemPlugins() {
		return "true".equals(System.getProperty("lolixl.overwriteSystemPlugins"));
	}

	private void init() {
		try {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/org.to2mbn.lolixl.init.plugins.list"), "UTF-8"))) {
				String line;
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if (line.isEmpty() || line.startsWith("#"))
						continue;
					String[] splited = line.split(":", 2);
					String groupId = splited[0];
					String artifactId = splited[1];
					if (shouldOverwriteSystemPlugins()) {
						pluginManager.getLocalRepository().getRepository().deleteArtifactAllVersions(groupId, artifactId)
								.handle((result, ex) -> installInitPlugin(groupId, artifactId)).get().get();
					} else {
						installInitPlugin(groupId, artifactId).get();
					}

				}
			}
		} catch (Throwable e) {
			LOGGER.log(Level.SEVERE, "LoliXL couldn't initialize", e);
		}
	}

	private CompletableFuture<Plugin> installInitPlugin(String groupId, String artifactId) {
		return pluginManager.getRemoteRepository().getRepository().getVersioning(groupId, artifactId)
				.thenCompose(versioning -> pluginManager.install(new MavenArtifact(groupId, artifactId, versioning.getLatest())))
				.exceptionally(ex -> {
					LOGGER.log(Level.SEVERE, "Couldn't start init plugin " + groupId + ":" + artifactId, ex);
					return null;
				});
	}

}
