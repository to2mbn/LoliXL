package org.to2mbn.lolixl.plugin.maven;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

public class ArtifactVersioning implements Serializable {

	private static final long serialVersionUID = 1L;

	private String latest;
	private String snapshot;
	private Set<String> versions;

	public ArtifactVersioning(String latest, String snapshot, Set<String> versions) {
		this.latest = latest;
		this.snapshot = snapshot;
		this.versions = versions;
	}

	public String getLatest() {
		return latest;
	}

	public String getSnapshot() {
		return snapshot;
	}

	public Set<String> getVersions() {
		return versions;
	}

	@Override
	public String toString() {
		return String.format("ArtifactVersioning [latest=%s, snapshot=%s, versions=%s]", latest, snapshot, versions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(latest, snapshot, versions);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof ArtifactVersioning) {
			ArtifactVersioning another = (ArtifactVersioning) obj;
			return Objects.equals(latest, another.latest)
					&& Objects.equals(snapshot, another.snapshot)
					&& Objects.equals(versions, another.versions);
		}
		return false;
	}

}
