package org.to2mbn.lolixl.utils;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleRevision;
import java.net.URL;
import java.util.Objects;

public final class BundleUtils {

	private BundleUtils() {}

	public static void waitBundleStarted(Bundle bundle) {
		if ((bundle.adapt(BundleRevision.class).getTypes() & BundleRevision.TYPE_FRAGMENT) != 0) {
			return;
		}

		BundleContext ctx;
		int state;

		// 自旋锁
		for (;;) {
			state = bundle.getState();
			if (state != Bundle.STARTING && state != Bundle.ACTIVE) {
				return;
			}
			ctx = bundle.getBundleContext();
			if (ctx == null) {
				Thread.yield();
			} else {
				return;
			}
		}
	}

	public static URL getResourceFromBundle(Class<?> clazz, String location) {
		Objects.requireNonNull(clazz);
		Objects.requireNonNull(location);
		return FrameworkUtil.getBundle(clazz).getResource(location);
	}

}
