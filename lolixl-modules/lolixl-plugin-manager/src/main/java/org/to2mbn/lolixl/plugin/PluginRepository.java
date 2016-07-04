package org.to2mbn.lolixl.plugin;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 代表一个插件仓库（可能是远程或本地）
 * 
 * @author yushijinhun
 */
public interface PluginRepository {

	/**
	 * 从仓库中查询一个插件的信息。
	 * 
	 * @param artifact 插件的Maven信息
	 * @return 插件信息，可能不存在
	 * @async
	 */
	CompletableFuture<Optional<PluginDescription>> getPluginDescription(MavenArtifact artifact);

}
