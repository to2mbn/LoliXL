package org.to2mbn.lolixl.main;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

public class OSGiListener implements BundleListener, ServiceListener, FrameworkListener {

	private static final Logger LOGGER_FRAMEWORK = Logger.getLogger("org.apache.felix.framework.event.framework");
	private static final Logger LOGGER_BUNDLE = Logger.getLogger("org.apache.felix.framework.event.bundle");
	private static final Logger LOGGER_SERVICE = Logger.getLogger("org.apache.felix.framework.event.service");

	@Override
	public void frameworkEvent(FrameworkEvent event) {
		Level level;
		String type;
		switch (event.getType()) {
			case FrameworkEvent.STARTED:
				level = Level.INFO;
				type = "STARTED";
				break;
			case FrameworkEvent.ERROR:
				level = Level.SEVERE;
				type = "ERROR";
				break;
			case FrameworkEvent.WARNING:
				level = Level.WARNING;
				type = "WARNING";
				break;
			case FrameworkEvent.INFO:
				level = Level.INFO;
				type = "INFO";
				break;
			case FrameworkEvent.PACKAGES_REFRESHED:
				level = Level.FINE;
				type = "PACKAGES_REFRESHED";
				break;
			case FrameworkEvent.STARTLEVEL_CHANGED:
				level = Level.FINE;
				type = "STARTLEVEL_CHANGED";
				break;
			case FrameworkEvent.STOPPED:
				level = Level.INFO;
				type = "STOPPED";
				break;
			case FrameworkEvent.STOPPED_BOOTCLASSPATH_MODIFIED:
				level = Level.INFO;
				type = "STOPPED_BOOTCLASSPATH_MODIFIED";
				break;
			case FrameworkEvent.STOPPED_UPDATE:
				level = Level.INFO;
				type = "STOPPED_UPDATE";
				break;
			case FrameworkEvent.WAIT_TIMEDOUT:
				level = Level.FINE;
				type = "WAIT_TIMEDOUT";
				break;
			default:
				level = Level.OFF;
				type = "<Unknown type: " + event.getType() + ">";
				break;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(type);
		if (event.getBundle() != null) {
			sb.append(" bundle=[")
					.append(event.getBundle())
					.append("]");
		}
		LOGGER_FRAMEWORK.log(level, sb.toString(), event.getThrowable());
	}

	@Override
	public void serviceChanged(ServiceEvent event) {
		String type;
		Level level = Level.FINE;
		switch (event.getType()) {
			case ServiceEvent.REGISTERED:
				type = "REGISTERED";
				break;
			case ServiceEvent.MODIFIED:
				type = "MODIFIED";
				break;
			case ServiceEvent.MODIFIED_ENDMATCH:
				type = "MODIFIED_ENDMATCH";
				break;
			case ServiceEvent.UNREGISTERING:
				type = "UNREGISTERING";
				break;
			default:
				level = Level.OFF;
				type = "<Unknown type: " + event.getType() + ">";
				break;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(type);
		if (event.getServiceReference() != null) {
			sb.append(" serviceRef=")
					.append(event.getServiceReference());
		}
		LOGGER_SERVICE.log(level, sb.toString());
	}

	@Override
	public void bundleChanged(BundleEvent event) {
		String type;
		Level level;
		switch (event.getType()) {
			case BundleEvent.INSTALLED:
				level = Level.INFO;
				type = "INSTALLED";
				break;
			case BundleEvent.RESOLVED:
				level = Level.FINE;
				type = "RESOLVED";
				break;
			case BundleEvent.LAZY_ACTIVATION:
				level = Level.FINE;
				type = "LAZY_ACTIVATION";
				break;
			case BundleEvent.STARTING:
				level = Level.FINE;
				type = "STARTING";
				break;
			case BundleEvent.STARTED:
				level = Level.INFO;
				type = "STARTED";
				break;
			case BundleEvent.STOPPING:
				level = Level.FINE;
				type = "STOPPING";
				break;
			case BundleEvent.STOPPED:
				level = Level.INFO;
				type = "STOPPED";
				break;
			case BundleEvent.UPDATED:
				level = Level.INFO;
				type = "UPDATED";
				break;
			case BundleEvent.UNRESOLVED:
				level = Level.FINE;
				type = "UNRESOLVED";
				break;
			case BundleEvent.UNINSTALLED:
				level = Level.INFO;
				type = "UNINSTALLED";
				break;
			default:
				level = Level.OFF;
				type = "<Unknown type: " + event.getType() + ">";
				break;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(type);
		if (event.getBundle() != null) {
			sb.append(" bundle=[")
					.append(event.getBundle())
					.append("]");
		}
		if (event.getOrigin() != null) {
			sb.append(" origin=[")
					.append(event.getOrigin())
					.append("]");
		}
		LOGGER_BUNDLE.log(level, sb.toString());
	}

}
