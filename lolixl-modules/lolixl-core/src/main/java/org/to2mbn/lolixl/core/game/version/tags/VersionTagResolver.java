package org.to2mbn.lolixl.core.game.version.tags;

import org.to2mbn.jmccc.version.Version;
import javafx.collections.ObservableSet;

public interface VersionTagResolver {

	ObservableSet<VersionTag> resolveTags(Version version);
}
