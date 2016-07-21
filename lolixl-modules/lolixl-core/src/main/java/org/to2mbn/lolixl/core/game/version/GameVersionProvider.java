package org.to2mbn.lolixl.core.game.version;

import java.util.Map;
import org.to2mbn.lolixl.core.ui.DisplayableItem;

public interface GameVersionProvider extends DisplayableItem {

	String PROPERTY_PROVIDER_LOCATION = "org.to2mbn.lolixl.core.provider";

	Map<String, GameVersion> listVersions();

}
