package org.to2mbn.lolixl.core.version;

import java.nio.file.Path;
import org.to2mbn.jmccc.version.Version;

public interface GameVersion {

	String getName();

	Path getMinecraftDirectory();
	Version getLaunchableVersion();

}
