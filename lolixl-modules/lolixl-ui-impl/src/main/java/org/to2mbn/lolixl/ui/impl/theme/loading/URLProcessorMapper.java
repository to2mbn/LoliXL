package org.to2mbn.lolixl.ui.impl.theme.loading;

import org.to2mbn.lolixl.ui.theme.loading.ThemeLoadingProcessor;

import java.io.InputStream;
import java.net.URL;
import java.util.function.Predicate;
import java.util.function.Supplier;

class URLProcessorMapper {
	private final Class<? extends ThemeLoadingProcessor> processorType;
	private final Predicate<URL> checker;
	private final Supplier<? extends ThemeLoadingProcessor<? extends InputStream>> processorSupplier;

	URLProcessorMapper(Class<? extends ThemeLoadingProcessor> _processorType, Predicate<URL> _checker, Supplier<? extends ThemeLoadingProcessor<? extends InputStream>> _processorSupplier) {
		processorType = _processorType;
		checker = _checker;
		processorSupplier = _processorSupplier;
	}

	Class<? extends ThemeLoadingProcessor> getProcessorType() {
		return processorType;
	}

	Predicate<URL> getChecker() {
		return checker;
	}

	Supplier<? extends ThemeLoadingProcessor<? extends InputStream>> getProcessorSupplier() {
		return processorSupplier;
	}
}
