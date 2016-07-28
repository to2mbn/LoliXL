package org.to2mbn.lolixl.ui.theme.loading;

import org.to2mbn.lolixl.ui.theme.Theme;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface ThemeLoadingService {
	Optional<Theme> loadFromURL(URL url) throws IOException;

	<T extends InputStream> void registerProcessor(Class<? extends ThemeLoadingProcessor<T>> processorType, Predicate<URL> checker, Supplier<ThemeLoadingProcessor<T>> processorSupplier);

	<T extends ThemeLoadingProcessor> void unregisterProcessor(Class<T> processorType);

	Optional<Theme> findThemeById(String id);

	Theme[] getLoadedThemes();
}
