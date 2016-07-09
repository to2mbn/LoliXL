package org.to2mbn.lolixl.plugin.impl.maven;

import java.io.Serializable;
import java.util.Set;
import org.to2mbn.lolixl.plugin.PluginDescription;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;

public class PluginDescriptionImpl implements PluginDescription, Serializable {

	private static final long serialVersionUID = 1L;

	private MavenArtifact artifact;
	private Set<MavenArtifact> dependencies;

	public PluginDescriptionImpl(MavenArtifact artifact, Set<MavenArtifact> dependencies) {
		this.artifact = artifact;
		this.dependencies = dependencies;
	}

	@Override
	public MavenArtifact getArtifact() {
		return artifact;
	}

	@Override
	public Set<MavenArtifact> getDependencies() {
		return dependencies;
	}

	@Override
	public String toString() {
		return String.format("PluginDescription [artifact=%s, dependencies=%s]", artifact, dependencies);
	}

}
