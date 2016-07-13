package org.to2mbn.lolixl.plugin.maven;

import java.io.Serializable;
import java.util.Objects;
import java.util.SortedSet;

public class ArtifactVersioning implements Serializable {

	private static final long serialVersionUID = 1L;

	private String latest;
	private String snapshot;
	private SortedSet<String> versions;

	public ArtifactVersioning(String latest, String snapshot, SortedSet<String> versions) {
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

	/**
	 * @return 所有的版本，降序排列
	 */
	public SortedSet<String> getVersions() {
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
