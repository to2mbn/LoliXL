package org.to2mbn.lolixl.core.download.game;

import java.util.List;

public interface VersionsGroup<VER extends DownloadableVersion> {

	String getLocalizedName();

	List<VER> getVersions();

}
