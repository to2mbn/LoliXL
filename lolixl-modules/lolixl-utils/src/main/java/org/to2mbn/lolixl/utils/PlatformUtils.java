package org.to2mbn.lolixl.utils;

import java.lang.management.ManagementFactory;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

public final class PlatformUtils {

	private PlatformUtils() {}

	private static Object getOperatingSystemMXBeanAttribute(String attribute) throws JMException {
		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
		return mbeanServer.getAttribute(new ObjectName("java.lang", "type", "OperatingSystem"), attribute);
	}

	public static long getCommittedVirtualMemorySize() throws JMException {
		return (long) getOperatingSystemMXBeanAttribute("CommittedVirtualMemorySize");
	}

	public static long getTotalSwapSpaceSize() throws JMException {
		return (long) getOperatingSystemMXBeanAttribute("TotalSwapSpaceSize");
	}

	public static long getFreeSwapSpaceSize() throws JMException {
		return (long) getOperatingSystemMXBeanAttribute("FreeSwapSpaceSize");
	}

	public static long getProcessCpuTime() throws JMException {
		return (long) getOperatingSystemMXBeanAttribute("ProcessCpuTime");
	}

	public static long getFreePhysicalMemorySize() throws JMException {
		return (long) getOperatingSystemMXBeanAttribute("FreePhysicalMemorySize");
	}

	public static long getTotalPhysicalMemorySize() throws JMException {
		return (long) getOperatingSystemMXBeanAttribute("TotalPhysicalMemorySize");
	}

	public static double getSystemCpuLoad() throws JMException {
		return (double) getOperatingSystemMXBeanAttribute("SystemCpuLoad");
	}

	public static double getProcessCpuLoad() throws JMException {
		return (double) getOperatingSystemMXBeanAttribute("ProcessCpuLoad");
	}

}
