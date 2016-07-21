package org.to2mbn.lolixl.core.game.version;

import java.nio.file.Path;
import java.util.List;
import org.to2mbn.lolixl.core.ui.Aliasable;
import org.to2mbn.lolixl.core.ui.DisplayableTile;

public interface GameVersionProvider extends DisplayableTile, Aliasable {

	String PROPERTY_PROVIDER_LOCATION = "org.to2mbn.lolixl.core.game.version.provider";

	List<GameVersion> getVersions();
	Path getMinecraftDirectory();

}
