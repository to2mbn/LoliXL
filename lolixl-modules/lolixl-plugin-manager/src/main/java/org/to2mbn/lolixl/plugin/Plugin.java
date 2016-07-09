package org.to2mbn.lolixl.plugin;

import java.util.Set;
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
	 * @return 插件所依赖的Bundle，在依赖关系未解决的情况下，该方法返回的依赖是不完整的
	 */
	Set<Bundle> getDependentBundles();

	/**
	 * 将该插件禁用并从内存中卸载。
	 * 
	 * @return void
	 */
	CompletableFuture<Void> unload();

}
