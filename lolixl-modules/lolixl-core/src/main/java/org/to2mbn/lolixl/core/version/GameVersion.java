package org.to2mbn.lolixl.core.version;

import java.nio.file.Path;
import org.to2mbn.jmccc.version.Version;
import javafx.scene.control.Button;

public interface GameVersion {

	String getName();

	Path getMinecraftDirectory();
	Version getLaunchableVersion();

	// TODO: Change to our Tile class?
	Button createTile();
}
