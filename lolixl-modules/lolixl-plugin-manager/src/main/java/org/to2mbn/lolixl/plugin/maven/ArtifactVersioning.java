package org.to2mbn.lolixl.plugin.maven;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

public class ArtifactVersioning implements Serializable {

	private static final long serialVersionUID = 1L;

	private String release;
	private String snapshot;
	private Set<String> versions;

	public ArtifactVersioning(String release, String snapshot, Set<String> versions) {
		this.release = release;
		this.snapshot = snapshot;
		this.versions = versions;
	}

	public String getRelease() {
		return release;
	}

	public String getSnapshot() {
		return snapshot;
	}

	public Set<String> getVersions() {
		return versions;
	}

	@Override
	public String toString() {
		return String.format("ArtifactVersioning [release=%s, snapshot=%s, versions=%s]", release, snapshot, versions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(release, snapshot, versions);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof ArtifactVersioning) {
			ArtifactVersioning another = (ArtifactVersioning) obj;
			return Objects.equals(release, another.release)
					&& Objects.equals(snapshot, another.snapshot)
					&& Objects.equals(versions, another.versions);
		}
		return false;
	}

}
