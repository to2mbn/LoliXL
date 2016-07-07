package org.to2mbn.lolixl.plugin.util;

import org.to2mbn.lolixl.plugin.MavenArtifact;

public final class MavenUtils {

	private MavenUtils() {}

	public static String getArtifactFileName(MavenArtifact artifact, String classifier, String type) {
		StringBuilder sb = new StringBuilder();
		sb.append(artifact.getArtifactId())
				.append('-')
				.append(artifact.getVersion());
		if (classifier != null) {
			sb.append('-')
					.append(classifier);
		}
		sb.append('.')
				.append(type == null ? "jar" : type);
		return sb.toString();
	}

}
