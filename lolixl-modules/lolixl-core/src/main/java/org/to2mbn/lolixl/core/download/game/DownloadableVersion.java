package org.to2mbn.lolixl.core.download.game;

import java.util.Set;
import javafx.scene.control.Button;

public interface DownloadableVersion {

	String getLocalizedName();

	Set<String> getSearchTags();

	// TODO: Change to our Tile class?
	Button createTile();

}
