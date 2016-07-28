package org.to2mbn.lolixl.ui.theme.loading;

import org.to2mbn.lolixl.ui.theme.Theme;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface ThemeLoadingProcessor<T extends InputStream> {
	Predicate<URL> getChecker();

	Supplier<ThemeLoadingProcessor<T>> getProcessorFactory();

	Theme process(URL baseUrl, T stream) throws IOException;
}
