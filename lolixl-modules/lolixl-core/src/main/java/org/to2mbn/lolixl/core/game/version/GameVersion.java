package org.to2mbn.lolixl.core.game.version;

import java.nio.file.Path;
import org.to2mbn.jmccc.version.Version;
import org.to2mbn.lolixl.core.ui.Aliasable;
import org.to2mbn.lolixl.core.ui.DisplayableTile;

public interface GameVersion extends DisplayableTile, Aliasable {

	String getVersionNumber();

	Path getMinecraftDirectory();
	Version getLaunchableVersion();

}
