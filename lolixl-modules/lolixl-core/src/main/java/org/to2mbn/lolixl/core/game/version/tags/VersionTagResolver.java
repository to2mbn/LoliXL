package org.to2mbn.lolixl.core.game.version.tags;

import org.to2mbn.jmccc.version.Version;
import java.util.Set;

public interface VersionTagResolver {

	Set<VersionTag> resolveTags(Version version);
}
