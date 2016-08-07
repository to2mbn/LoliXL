package org.to2mbn.lolixl.ui.impl.version;

import java.io.Serializable;
import java.util.Objects;

public class SelectedGameVersion implements Serializable {

	private static final long serialVersionUID = 1L;

	private String versionName;
	private String providerName;

	public SelectedGameVersion(String versionName, String providerName) {
		this.versionName = versionName;
		this.providerName = providerName;
	}

	public String getVersionName() {
		return versionName;
	}

	public String getProviderName() {
		return providerName;
	}

	@Override
	public String toString() {
		return String.format("[versionName=%s, providerName=%s]", versionName, providerName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(versionName, providerName);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof SelectedGameVersion) {
			SelectedGameVersion another = (SelectedGameVersion) obj;
			return Objects.equals(providerName, another.providerName) &&
					Objects.equals(versionName, another.versionName);
		}
		return false;
	}

}
