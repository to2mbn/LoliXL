package org.to2mbn.lolixl.ui.impl.theme.loading;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.to2mbn.lolixl.ui.theme.Theme;
import org.to2mbn.lolixl.ui.theme.loading.ThemeLoadingProcessor;
import org.to2mbn.lolixl.ui.theme.loading.ThemeLoadingService;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;

@Component(immediate = true)
@Service({ ThemeLoadingService.class })
public class ThemeLoadingServiceImpl implements ThemeLoadingService {
	private static final Logger LOGGER = Logger.getLogger(ThemeLoadingServiceImpl.class.getCanonicalName());

	private ServiceTracker<ThemeLoadingProcessor, ThemeLoadingProcessor> processorTracker;
	private List<URLProcessorMapper> mappers;
	private Queue<Theme> loadedThemes;

	@Activate
	public void active(ComponentContext compCtx) {
		mappers = new LinkedList<>();
		loadedThemes = new ConcurrentLinkedQueue<>();
		processorTracker = new ServiceTracker<>(compCtx.getBundleContext(), ThemeLoadingProcessor.class, new ServiceTrackerCustomizer<ThemeLoadingProcessor, ThemeLoadingProcessor>() {
			@Override
			public ThemeLoadingProcessor addingService(ServiceReference<ThemeLoadingProcessor> serviceReference) {
				ThemeLoadingProcessor processor = compCtx.getBundleContext().getService(serviceReference);
				registerProcessor(processor.getClass(), processor.getChecker(), processor.getProcessorFactory());
				return processor;
			}

			@Override
			public void modifiedService(ServiceReference<ThemeLoadingProcessor> serviceReference, ThemeLoadingProcessor themeLoadingProcessor) {}

			@Override
			public void removedService(ServiceReference<ThemeLoadingProcessor> serviceReference, ThemeLoadingProcessor themeLoadingProcessor) {
				unregisterProcessor(themeLoadingProcessor.getClass());
			}
		});
		processorTracker.open();
	}

	@Override
	public Optional<Theme> loadFromURL(URL url) throws IOException {
		Optional<Theme> theme = Optional.empty();
		for (URLProcessorMapper mapper : mappers) {
			if (mapper.checker.test(url)) {
				ThemeLoadingProcessor processor = mapper.processorSupplier.get();
				LOGGER.info("Loading theme from '" + url.toExternalForm() + "' using '" + processor.getClass().getCanonicalName() + "'");
				theme = Optional.of(processor.process(url));
				if (theme.isPresent()) {
					loadedThemes.offer(theme.get());
				}
			}
		}
		return theme;
	}

	@Override
	public void registerProcessor(Class<? extends ThemeLoadingProcessor> processorType, Predicate<URL> checker, Supplier<? extends ThemeLoadingProcessor> processorSupplier) {
		LOGGER.info("Registering processor '" + processorType.getCanonicalName() + "'");
		mappers.add(new URLProcessorMapper(processorType, checker, processorSupplier));
	}

	@Override
	public void unregisterProcessor(Class<? extends ThemeLoadingProcessor> processorType) {
		LOGGER.info("Removing processor '" + processorType.getCanonicalName() + "'");
		mappers.removeIf(mapper -> mapper.processorType == processorType);
	}

	@Override
	public Optional<Theme> findThemeById(String id) {
		Objects.requireNonNull(id);
		if (id.isEmpty()) {
			return Optional.empty();
		}
		for (Theme theme : loadedThemes) {
			if (id.equals(theme.getMeta().get(Theme.META_KEY_ID))) {
				return Optional.of(theme);
			}
		}
		return Optional.empty();
	}

	@Override
	public Theme[] getLoadedThemes() {
		return loadedThemes.toArray(new Theme[loadedThemes.size()]);
	}
}
