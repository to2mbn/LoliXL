package org.to2mbn.lolixl.ui.theme.loading;

import org.to2mbn.lolixl.ui.theme.Theme;

import java.io.IOException;
import java.net.URL;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface ThemeLoadingProcessor {
	Predicate<URL> getChecker();

	Supplier<ThemeLoadingProcessor> getProcessorFactory();

	Theme process(URL baseUrl) throws IOException;
}
