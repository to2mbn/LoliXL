package org.to2mbn.lolixl.core.game.configuration;

import org.to2mbn.jmccc.option.LaunchOption;
import org.to2mbn.lolixl.core.game.auth.AuthenticationProfile;
import org.to2mbn.lolixl.core.game.version.GameVersion;
import org.to2mbn.lolixl.utils.Aliasable;
import org.to2mbn.lolixl.utils.ObservableContextAware;

public interface GameConfiguration extends java.io.Serializable, Aliasable, ObservableContextAware {

	LaunchOption process(AuthenticationProfile<?> authentication, GameVersion versionToLaunch);

}
