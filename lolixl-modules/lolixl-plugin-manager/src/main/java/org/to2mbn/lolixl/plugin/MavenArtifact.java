package org.to2mbn.lolixl.plugin;

import java.io.Serializable;
import java.util.Objects;

/**
 * 用GAV(groupId, artifactId, version)描述的一个Maven构件。
 * 
 * @author yushijinhun
 */
public class MavenArtifact implements Serializable {

	private static final long serialVersionUID = 1L;

	private String groupId;
	private String artifactId;
	private String version;

	public MavenArtifact(String groupId, String artifactId, String version) {
		this.groupId = Objects.requireNonNull(groupId);
		this.artifactId = Objects.requireNonNull(artifactId);
		this.version = Objects.requireNonNull(version);
	}

	// Getters
	// @formatter:off
	public String getGroupId() { return groupId; }
	public String getArtifactId() { return artifactId; }
	public String getVersion() { return version; }
	// @formatter:on

	@Override
	public int hashCode() {
		return Objects.hash(groupId, artifactId, version);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof MavenArtifact) {
			MavenArtifact another = (MavenArtifact) obj;
			return groupId.equals(another.groupId)
					&& artifactId.equals(another.artifactId)
					&& version.equals(another.version);
		}
		return false;
	}

	@Override
	public String toString() {
		return groupId + ":" + artifactId + ":" + version;
	}

}
