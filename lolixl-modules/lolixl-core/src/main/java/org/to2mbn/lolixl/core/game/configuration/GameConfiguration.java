package org.to2mbn.lolixl.core.game.configuration;

import org.to2mbn.jmccc.option.LaunchOption;
import org.to2mbn.lolixl.core.game.auth.AuthenticationProfile;
import org.to2mbn.lolixl.core.game.version.GameVersion;
import org.to2mbn.lolixl.core.ui.Aliasable;

public interface GameConfiguration extends java.io.Serializable, Aliasable {

	LaunchOption process(AuthenticationProfile<?> authentication, GameVersion versionToLaunch);

}
