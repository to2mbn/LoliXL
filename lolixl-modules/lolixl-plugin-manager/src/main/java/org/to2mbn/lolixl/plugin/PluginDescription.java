package org.to2mbn.lolixl.plugin;

import java.util.Set;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;

public interface PluginDescription {

	/**
	 * @return 插件对应的Maven信息
	 */
	MavenArtifact getArtifact();

	/**
	 * @return 该插件的依赖
	 */
	Set<MavenArtifact> getDependencies();

}
