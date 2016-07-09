package org.to2mbn.lolixl.plugin;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;
import org.to2mbn.lolixl.plugin.maven.MavenRepository;

/**
 * 代表一个插件仓库（可能是远程或本地）
 * 
 * @author yushijinhun
 */
public interface PluginRepository {

	/**
	 * 从仓库中查询一个插件的元数据。
	 * <p>
	 * 该方法出于效率将不会对插件元数据的数字签名进行校验，因此返回的元数据不具有可靠性。
	 * 
	 * @param artifact 插件的Maven信息
	 * @return 插件元数据，可能不存在
	 */
	CompletableFuture<Optional<PluginDescription>> getPluginDescription(MavenArtifact artifact);

	MavenRepository getRepository();

}
