package org.to2mbn.lolixl.core.download.game;

import java.util.List;
import javafx.scene.layout.Region;

public interface VersionDescription {

	String getTitle();

	List<DownloadAction> getDownloadActions();

	Region createDescriptionComponent();

}
