package org.to2mbn.lolixl.plugin.impl.resolver;

import org.to2mbn.lolixl.plugin.maven.MavenArtifact;

abstract public class DependencyAction {

	public final MavenArtifact artifact;

	public DependencyAction(MavenArtifact artifact) {
		this.artifact = artifact;
	}

	public static class InstallAction extends DependencyAction {

		public InstallAction(MavenArtifact artifact) {
			super(artifact);
		}
	}

	public static class UninstallAction extends DependencyAction {

		public UninstallAction(MavenArtifact artifact) {
			super(artifact);
		}
	}

	public static class UpdateAction extends DependencyAction {

		public final MavenArtifact targetArtifact;

		public UpdateAction(MavenArtifact artifact, MavenArtifact targetArtifact) {
			super(artifact);
			this.targetArtifact = targetArtifact;
		}

	}

}
