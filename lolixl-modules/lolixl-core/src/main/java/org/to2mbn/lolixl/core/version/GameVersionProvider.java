package org.to2mbn.lolixl.core.version;

import java.util.Map;

public interface GameVersionProvider {

	String PROPERTY_PROVIDER_LOCATION = "org.to2mbn.lolixl.core.provider";

	String getLocalizedName();

	Map<String, GameVersion> listVersions();

}
