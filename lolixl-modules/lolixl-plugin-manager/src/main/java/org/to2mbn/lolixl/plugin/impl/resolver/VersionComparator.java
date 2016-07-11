package org.to2mbn.lolixl.plugin.impl.resolver;

import java.util.Comparator;

public class VersionComparator implements Comparator<String> {

	@Override
	public int compare(String a, String b) {
		return new ComparableVersion(a).compareTo(new ComparableVersion(b));
	}

}
