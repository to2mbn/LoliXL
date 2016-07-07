package org.to2mbn.lolixl.plugin.maven;

import java.io.Serializable;
import java.util.Objects;

public class ArtifactSnapshot implements Serializable {

	private static final long serialVersionUID = 1L;

	private String timestamp;
	private int buildNumber;

	public ArtifactSnapshot(String timestamp, int buildNumber) {
		this.timestamp = timestamp;
		this.buildNumber = buildNumber;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public int getBuildNumber() {
		return buildNumber;
	}

	@Override
	public String toString() {
		return String.format("ArtifactSnapshot [timestamp=%s, buildNumber=%s]", timestamp, buildNumber);
	}

	@Override
	public int hashCode() {
		return Objects.hash(timestamp, buildNumber);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof ArtifactSnapshot) {
			ArtifactSnapshot another = (ArtifactSnapshot) obj;
			return Objects.equals(timestamp, another.timestamp)
					&& Objects.equals(buildNumber, another.buildNumber);
		}
		return false;
	}

}
