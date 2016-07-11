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
	 * 将该插件禁用并从内存中卸载。
	 * 
	 * @return void
	 */
	CompletableFuture<Void> unload();

}
