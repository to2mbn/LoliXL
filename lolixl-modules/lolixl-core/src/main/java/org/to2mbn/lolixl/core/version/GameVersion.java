package org.to2mbn.lolixl.core.version;

import java.nio.file.Path;
import java.util.Set;
import org.to2mbn.jmccc.version.Version;

public interface GameVersion {

	String getName();
	Set<String> getTags();

	Path getMinecraftDirectory();
	Version getLaunchableVersion();

}
