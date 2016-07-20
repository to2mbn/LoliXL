package org.to2mbn.lolixl.core.download.game;

import java.util.List;

public interface VersionDescription {

	String getTitle();

	List<DownloadAction> getDownloadActions();

}
