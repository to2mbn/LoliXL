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
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Component(immediate = true)
@Service({ ThemeLoadingService.class })
public class ThemeLoadingServiceImpl implements ThemeLoadingService {
	private ServiceTracker<ThemeLoadingProcessor, ThemeLoadingProcessor> processorTracker;
	private List<URLProcessorMapper> mappers;
	private Queue<Theme> loadedThemes;

	@Activate
	public void active(ComponentContext compCtx) {
		mappers = new LinkedList<>();
		loadedThemes = new ConcurrentLinkedQueue<>();
		processorTracker = new ServiceTracker<>(compCtx.getBundleContext(), ThemeLoadingProcessor.class, new ServiceTrackerCustomizer<ThemeLoadingProcessor, ThemeLoadingProcessor>() {
			@Override
			public ThemeLoadingProcessor<?> addingService(ServiceReference<ThemeLoadingProcessor> serviceReference) {
				ThemeLoadingProcessor processor = compCtx.getBundleContext().getService(serviceReference);
				mappers.add(new URLProcessorMapper(processor.getClass(), processor.getChecker(), processor.getProcessorFactory()));
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
			if (mapper.getChecker().test(url)) {
				ThemeLoadingProcessor processor = mapper.getProcessorSupplier().get();
				theme = Optional.of(processor.process(url, url.openStream()));
			}
		}
		return theme;
	}

	@Override
	public <T extends InputStream> void registerProcessor(Class<? extends ThemeLoadingProcessor<T>> processorType, Predicate<URL> checker, Supplier<ThemeLoadingProcessor<T>> processorSupplier) {
		mappers.add(new URLProcessorMapper(processorType, checker, processorSupplier));
	}

	@Override
	public <T extends ThemeLoadingProcessor> void unregisterProcessor(Class<T> processorType) {
		mappers.removeIf(mapper -> mapper.getProcessorType() == processorType);
	}

	@Override
	public Optional<Theme> findThemeById(String id) {
		Objects.requireNonNull(id);
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
