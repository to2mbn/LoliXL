package org.to2mbn.lolixl.plugin;

import java.util.concurrent.CompletableFuture;

/**
 * 代表远程插件仓库。
 * 
 * @author yushijinhun
 */
public interface RemotePluginRepository extends PluginRepository {

	/**
	 * 从远程仓库中下载一个插件到本地。
	 * <p>
	 * 该方法不会下载插件的依赖，也不会将插件载入内存。<br>
	 * 该方法可能会出现异常，如插件不存在。
	 * 
	 * @param artifact 插件的Maven信息
	 * @return void
	 */
	CompletableFuture<Void> downloadPlugin(MavenArtifact artifact);

}
