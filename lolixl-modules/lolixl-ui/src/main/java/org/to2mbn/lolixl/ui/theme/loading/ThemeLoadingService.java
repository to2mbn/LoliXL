package org.to2mbn.lolixl.ui.theme.loading;

import org.to2mbn.lolixl.ui.theme.Theme;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface ThemeLoadingService {
	Optional<Theme> loadFromURL(URL url) throws IOException;

	void registerProcessor(Class<? extends ThemeLoadingProcessor> processorType, Predicate<URL> checker, Supplier<? extends ThemeLoadingProcessor> processorSupplier);

	void unregisterProcessor(Class<? extends ThemeLoadingProcessor> processorType);

	Optional<Theme> findThemeById(String id);

	Theme[] getLoadedThemes();
}
