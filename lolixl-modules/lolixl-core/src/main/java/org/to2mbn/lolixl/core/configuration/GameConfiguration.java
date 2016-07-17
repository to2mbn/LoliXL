package org.to2mbn.lolixl.core.configuration;

import org.to2mbn.jmccc.option.LaunchOption;
import org.to2mbn.lolixl.core.version.GameVersion;

public interface GameConfiguration extends java.io.Serializable {

	LaunchOption process(GameVersion versionToLaunch);

}
