package org.to2mbn.lolixl.ui.impl.util;

import static java.util.stream.Collectors.toList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class CssUtils {

	private CssUtils() {}

	public static String toCssUrl(String cssLocation) {
		Objects.requireNonNull(cssLocation);
		if ("true".equals(System.getProperty("lolixl.hackCss"))) {
			return "lolixlcss://current.classloader/" + cssLocation;
		} else {
			return cssLocation;
		}
	}

	public static List<String> mapCssToUrls(Collection<String> css) {
		return css.stream()
				.map(CssUtils::toCssUrl)
				.collect(toList());
	}

	public static List<String> mapCssToUrls(String... css) {
		String[] target = new String[css.length];
		for (int i = 0; i < css.length; i++) {
			target[i] = toCssUrl(css[i]);
		}
		return Arrays.asList(target);
	}

}
