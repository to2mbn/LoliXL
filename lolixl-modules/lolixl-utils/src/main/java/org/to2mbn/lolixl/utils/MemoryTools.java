package org.to2mbn.lolixl.utils;

import static java.lang.Math.*;
import javax.management.JMException;

public final class MemoryTools {

	private MemoryTools() {}

	public static int computeSuggestedXmx() throws JMException {
		int totalPhysicalMemorySize = (int) (PlatformUtils.getTotalPhysicalMemorySize() / 1024 / 1024);
		int mem = totalPhysicalMemorySize / 3;
		mem = max(1024, mem);
		mem = min(4096, mem);
		mem = min(totalPhysicalMemorySize, mem);
		return round(mem / 128) * 128;
	}

}
