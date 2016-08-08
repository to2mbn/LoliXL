package org.to2mbn.lolixl.core.game.version.tags;

import java.util.Set;
import org.to2mbn.jmccc.version.Version;

public interface VersionTagSolution {

	void resolve(Version version, Set<VersionTag> result);

}
