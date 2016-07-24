package org.to2mbn.lolixl.core.game.version;

import java.nio.file.Path;
import org.to2mbn.jmccc.version.Version;
import org.to2mbn.lolixl.ui.component.DisplayableTile;
import org.to2mbn.lolixl.utils.Aliasable;

public interface GameVersion extends DisplayableTile, Aliasable {

	String getVersionNumber();

	Path getMinecraftDirectory();
	Version getLaunchableVersion();

}
