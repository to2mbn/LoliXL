package org.to2mbn.lolixl.core.game.version;

import java.nio.file.Path;
import org.to2mbn.lolixl.ui.DisplayableTile;
import org.to2mbn.lolixl.utils.Aliasable;
import javafx.collections.ObservableList;

public interface GameVersionProvider extends DisplayableTile, Aliasable {

	String PROPERTY_PROVIDER_LOCATION = "org.to2mbn.lolixl.core.game.version.provider";

	ObservableList<GameVersion> getVersions();
	Path getMinecraftDirectory();

	void delete();

}
