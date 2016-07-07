package org.to2mbn.lolixl.plugin.util;

import org.to2mbn.lolixl.plugin.MavenArtifact;
import org.to2mbn.lolixl.plugin.maven.ArtifactSnapshot;
import org.to2mbn.lolixl.plugin.maven.IllegalVersionException;

public final class MavenUtils {

	private MavenUtils() {}

	public static String getArtifactFileName(MavenArtifact artifact, String classifier, String type) {
		if (isSnapshot(artifact.getVersion())) {
			throw new IllegalVersionException("Not a release version: " + artifact);
		}
		return getArtifactFileName0(artifact.getArtifactId(), artifact.getVersion(), classifier, type);
	}

	public static String getArtifactFileName(MavenArtifact artifact, ArtifactSnapshot snapshot, String classifier, String type) {
		if (!isSnapshot(artifact.getVersion())) {
			throw new IllegalVersionException("Not a snapshot version: " + artifact);
		}
		String artifactVersion = artifact.getVersion();
		String version0 = artifactVersion.substring(0, artifactVersion.length() - "SNAPSHOT".length()) + snapshot.getTimestamp() + "-" + snapshot.getBuildNumber();
		return getArtifactFileName0(artifact.getArtifactId(), version0, classifier, type);
	}

	private static String getArtifactFileName0(String artifactId, String version0, String classifier, String type) {
		StringBuilder sb = new StringBuilder();
		sb.append(artifactId)
				.append('-')
				.append(version0);
		if (classifier != null) {
			sb.append('-')
					.append(classifier);
		}
		sb.append('.')
				.append(type == null ? "jar" : type);
		return sb.toString();
	}

	public static boolean isSnapshot(String version) {
		return version.endsWith("-SNAPSHOT");
	}

}
