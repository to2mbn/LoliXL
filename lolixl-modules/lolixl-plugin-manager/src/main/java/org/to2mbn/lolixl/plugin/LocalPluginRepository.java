package org.to2mbn.lolixl.plugin;

import java.util.concurrent.CompletableFuture;
import org.to2mbn.lolixl.plugin.maven.LocalMavenRepository;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;
import org.to2mbn.lolixl.plugin.maven.MavenRepository;

/**
 * 代表本地插件仓库。
 * 
 * @author yushijinhun
 */
public interface LocalPluginRepository extends PluginRepository {

	CompletableFuture<Void> downloadBundle(MavenRepository from, MavenArtifact artifact);

	@Override
	LocalMavenRepository getRepository();

}
