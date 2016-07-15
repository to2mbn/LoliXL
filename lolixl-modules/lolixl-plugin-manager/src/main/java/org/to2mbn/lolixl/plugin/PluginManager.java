package org.to2mbn.lolixl.plugin;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;

/**
 * plugin-manager的门面类
 * 
 * @author yushijinhun
 */
public interface PluginManager {

	PluginService getService();
	LocalPluginRepository getLocalRepository();
	PluginRepository getRemoteRepository();

	CompletableFuture<Plugin> install(MavenArtifact artifact);
	CompletableFuture<Optional<MavenArtifact>> checkForUpdate(MavenArtifact artifact);
	CompletableFuture<Void> cleanup();

}
