package org.to2mbn.lolixl.main;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

public class FelixLoggerAdapter extends org.apache.felix.framework.Logger {

	private static final Logger LOGGER = Logger.getLogger("org.apache.felix.framework");

	@Override
	protected void doLog(int level, String msg, Throwable throwable) {
		doLog(null, null, level, msg, throwable);
	}

	@Override
	protected void doLog(Bundle bundle, @SuppressWarnings("rawtypes") ServiceReference sr, int level, String msg, Throwable throwable) {
		StringBuilder sb = new StringBuilder();

		Level julLevel;
		switch (level) {
			case LOG_DEBUG:
				julLevel = Level.FINE;
				break;
			case LOG_ERROR:
				julLevel = Level.SEVERE;
				break;
			case LOG_INFO:
				julLevel = Level.INFO;
				break;
			case LOG_WARNING:
				julLevel = Level.WARNING;
				break;
			default:
				julLevel = Level.OFF;
				sb.append("<Unknown level: ")
						.append(level)
						.append("> ");
		}
		if (bundle != null)
			sb.append("Bundle=[")
					.append(bundle)
					.append("] ");
		if (sr != null)
			sb.append("SvcRef=")
					.append(sr)
					.append(' ');
		sb.append(msg);
		LOGGER.log(julLevel, sb.toString(), throwable);
	}
}
