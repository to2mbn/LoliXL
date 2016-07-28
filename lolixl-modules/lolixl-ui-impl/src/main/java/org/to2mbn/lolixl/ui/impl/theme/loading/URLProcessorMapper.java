package org.to2mbn.lolixl.ui.impl.theme.loading;

import org.to2mbn.lolixl.ui.theme.loading.ThemeLoadingProcessor;

import java.net.URL;
import java.util.function.Predicate;
import java.util.function.Supplier;

class URLProcessorMapper {
	final Class<? extends ThemeLoadingProcessor> processorType;
	final Predicate<URL> checker;
	final Supplier<? extends ThemeLoadingProcessor> processorSupplier;

	URLProcessorMapper(Class<? extends ThemeLoadingProcessor> _processorType, Predicate<URL> _checker, Supplier<? extends ThemeLoadingProcessor> _processorSupplier) {
		processorType = _processorType;
		checker = _checker;
		processorSupplier = _processorSupplier;
	}
}
