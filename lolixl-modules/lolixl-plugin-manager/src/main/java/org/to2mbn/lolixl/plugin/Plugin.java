package org.to2mbn.lolixl.plugin;

import java.util.concurrent.CompletableFuture;
import org.osgi.framework.Bundle;

/**
 * 代表一个已经载入内存的插件。
 * 
 * @author yushijinhun
 */
public interface Plugin {

	/**
	 * @return 该插件的描述
	 */
	PluginDescription getDescription();

	/**
	 * @return 插件在OSGi中的Bundle
	 */
	Bundle getBundle();

	/**
	 * 将插件升级为指定版本。
	 * <p>
	 * 新版本的groupId、artifactId与原来相同，version为指定的updateTo。该插件必须已经在本地仓库中存在。
	 * 如果说升级过程中出现异常，则升级失败， 插件滚回原来的版本。
	 * 
	 * @param updateTo 要升级到的版本
	 * @return void
	 */
	CompletableFuture<Void> update(String updateTo);

	/**
	 * 将该插件禁用并从内存中卸载。
	 * 
	 * @return void
	 */
	CompletableFuture<Void> unload();
}
