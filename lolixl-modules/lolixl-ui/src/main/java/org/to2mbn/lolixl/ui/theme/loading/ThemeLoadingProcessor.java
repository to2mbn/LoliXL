package org.to2mbn.lolixl.ui.theme.loading;

import org.to2mbn.lolixl.ui.theme.Theme;

import java.io.IOException;
import java.net.URL;
import java.util.function.Predicate;

public interface ThemeLoadingProcessor {
	Predicate<URL> getChecker();

	Theme process(URL baseUrl) throws IOException;
}
