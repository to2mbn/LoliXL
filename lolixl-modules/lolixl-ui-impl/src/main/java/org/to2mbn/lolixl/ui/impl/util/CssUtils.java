package org.to2mbn.lolixl.ui.impl.util;

import static java.util.stream.Collectors.toList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.osgi.framework.Bundle;

public final class CssUtils {

	private CssUtils() {}

	public static String toCssUrl(Bundle bundle, String cssLocation) {
		Objects.requireNonNull(cssLocation);
		return "lolixlcss://" + bundle.getBundleId() + "/" + cssLocation;
	}

	public static List<String> mapCssToUrls(Bundle bundle, Collection<String> css) {
		return css.stream()
				.map(cssEntry -> toCssUrl(bundle, cssEntry))
				.collect(toList());
	}

	public static List<String> mapCssToUrls(Bundle bundle, String... css) {
		String[] target = new String[css.length];
		for (int i = 0; i < css.length; i++) {
			target[i] = toCssUrl(bundle, css[i]);
		}
		return Arrays.asList(target);
	}

}
