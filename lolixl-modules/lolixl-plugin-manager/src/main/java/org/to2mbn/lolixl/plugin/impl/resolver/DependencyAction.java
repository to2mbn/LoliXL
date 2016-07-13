package org.to2mbn.lolixl.plugin.impl.resolver;

import org.to2mbn.lolixl.plugin.maven.MavenArtifact;

abstract public class DependencyAction {

	public final MavenArtifact artifact;

	public DependencyAction(MavenArtifact artifact) {
		this.artifact = artifact;
	}

	abstract public DependencyAction revert();

	public static class InstallAction extends DependencyAction {

		public InstallAction(MavenArtifact artifact) {
			super(artifact);
		}

		@Override
		public DependencyAction revert() {
			return new UninstallAction(artifact);
		}

		@Override
		public String toString() {
			return String.format("install[%s]", artifact);
		}

	}

	public static class UninstallAction extends DependencyAction {

		public UninstallAction(MavenArtifact artifact) {
			super(artifact);
		}

		@Override
		public DependencyAction revert() {
			return new InstallAction(artifact);
		}

		@Override
		public String toString() {
			return String.format("uninstall[%s]", artifact);
		}
	}

	public static class UpdateAction extends DependencyAction {

		public final MavenArtifact targetArtifact;

		public UpdateAction(MavenArtifact artifact, MavenArtifact targetArtifact) {
			super(artifact);
			this.targetArtifact = targetArtifact;
		}

		@Override
		public DependencyAction revert() {
			return new UpdateAction(targetArtifact, artifact);
		}

		@Override
		public String toString() {
			return String.format("update[%s -> %s]", artifact, targetArtifact);
		}

	}

}
