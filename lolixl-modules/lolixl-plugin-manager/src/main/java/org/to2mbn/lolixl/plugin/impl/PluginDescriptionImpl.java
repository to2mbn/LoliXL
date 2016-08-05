package org.to2mbn.lolixl.plugin.impl;

import java.io.Serializable;
import java.util.Set;
import org.to2mbn.lolixl.plugin.PluginDescription;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;

public class PluginDescriptionImpl implements PluginDescription, Serializable {

	private static final long serialVersionUID = 1L;

	private MavenArtifact artifact;
	private Set<MavenArtifact> dependencies;
	private Set<String> languageFiles;

	public PluginDescriptionImpl(MavenArtifact artifact, Set<MavenArtifact> dependencies, Set<String> languageFiles) {
		this.artifact = artifact;
		this.dependencies = dependencies;
		this.languageFiles = languageFiles;
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
	public Set<String> getLanguageFiles() {
		return languageFiles;
	}

	@Override
	public String toString() {
		return String.format("PluginDescriptionImpl [artifact=%s, dependencies=%s, languageFiles=%s]", artifact, dependencies, languageFiles);
	}

}
