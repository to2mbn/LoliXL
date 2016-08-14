package org.to2mbn.lolixl.core.impl.version.tags;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.jmccc.version.Version;
import org.to2mbn.lolixl.core.game.version.tags.VersionTag;
import org.to2mbn.lolixl.core.game.version.tags.VersionTagSolution;

@Service({ VersionTagSolution.class })
@Component(immediate = true)
public class VersionTypeTagSolution implements VersionTagSolution {

	public static final int RANKING = 10_000_000;
	
	private static final String[][] types={
			{ "snapshot", "org.to2mbn.lolixl.core.impl.version.tags.snapshot" },
			{ "old-beta", "org.to2mbn.lolixl.core.impl.version.tags.beta" },
			{ "old-alpha", "org.to2mbn.lolixl.core.impl.version.tags.alpha" },
	};

	private Map<String, VersionTag> type2tag = new HashMap<>();

	@Activate
	public void active(ComponentContext compCtx) {
		for (String[] kvPair : types) {
			type2tag.put(kvPair[0], new VersionTag(kvPair[1], RANKING, Collections.singletonList("xl-version-tag-" + kvPair[0])));
		}
	}

	@Override
	public void resolve(Version version, Set<VersionTag> result) {
		VersionTag tag = type2tag.get(version.getType());
		if (tag != null) {
			result.add(tag);
		}
	}

}
