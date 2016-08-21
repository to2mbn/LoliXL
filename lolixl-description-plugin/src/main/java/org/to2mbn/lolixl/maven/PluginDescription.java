package org.to2mbn.lolixl.maven;

import java.io.Serializable;
import java.util.Set;

public class PluginDescription implements Serializable {

	private static final long serialVersionUID = 1L;

	private MavenArtifact artifact;
	private Set<MavenArtifact> dependencies;

	public PluginDescription(MavenArtifact artifact, Set<MavenArtifact> dependencies) {
		this.artifact = artifact;
		this.dependencies = dependencies;
	}

	public MavenArtifact getArtifact() {
		return artifact;
	}

	public Set<MavenArtifact> getDependencies() {
		return dependencies;
	}

	@Override
	public String toString() {
		return String.format("PluginDescription [artifact=%s, dependencies=%s]", artifact, dependencies);
	}

}
