package org.to2mbn.lolixl.plugin;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.osgi.framework.Bundle;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;

/**
 * 提供插件加载服务。
 * 
 * @author yushijinhun
 */
public interface PluginService {

	/**
	 * @return 所有已载入内存的插件
	 */
	Set<Plugin> getLoadedPlugins();

	/**
	 * 获取一个已载入内存的插件。
	 * 
	 * @param artifact 插件的Maven信息
	 * @return 插件
	 */
	Optional<Plugin> getPlugin(MavenArtifact artifact);
	Optional<Plugin> getPlugin(Bundle bundle);
	Optional<Bundle> getBundle(MavenArtifact artifact);
	Optional<MavenArtifact> getArtifact(Bundle bundle);

	/**
	 * 从插件仓库加载一个插件。
	 * <p>
	 * 加载过程中可能会出现异常，如插件不存在，则加载失败。
	 * 
	 * @param repository 插件仓库
	 * @param artifact 插件的Maven信息
	 * @return 插件
	 */
	CompletableFuture<Plugin> loadPlugin(PluginRepository repository, MavenArtifact artifact);

}
