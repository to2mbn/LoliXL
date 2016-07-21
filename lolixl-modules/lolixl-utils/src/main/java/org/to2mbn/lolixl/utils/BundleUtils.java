package org.to2mbn.lolixl.utils;

import org.osgi.framework.FrameworkUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

public final class BundleUtils {
	private BundleUtils() {
	}

	public static URL getResourceFromBundle(Class<?> clazz, String location) {
		Objects.requireNonNull(clazz);
		Objects.requireNonNull(location);
		return FrameworkUtil.getBundle(clazz).getResource(location);
	}

	public static InputStream getInputStreamFromBundle(Class<?> clazz, String location) throws IOException {
		URL url = getResourceFromBundle(clazz, location);
		return url != null ? url.openStream() : null;
	}
}
