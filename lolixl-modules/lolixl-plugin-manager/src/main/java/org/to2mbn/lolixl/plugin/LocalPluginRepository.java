package org.to2mbn.lolixl.plugin;

import java.util.concurrent.CompletableFuture;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;

/**
 * 代表本地插件仓库。
 * 
 * @author yushijinhun
 */
public interface LocalPluginRepository extends PluginRepository {

	/**
	 * 从仓库中删除插件。
	 * <p>
	 * 该方法可能会出现异常，如插件不存在，或还在使用中。
	 * 
	 * @param artifact 插件的Maven信息
	 * @return void
	 */
	CompletableFuture<Void> deletePlugin(MavenArtifact artifact);

}
