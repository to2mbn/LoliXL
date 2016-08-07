package org.to2mbn.lolixl.core.game.version;

import org.to2mbn.jmccc.version.Version;
import java.util.Set;

public interface VersionTagResolutionService {
	Set<String> resolveTags(Version version);
}
