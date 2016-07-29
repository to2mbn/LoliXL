package org.to2mbn.lolixl.ui.impl.theme.loading;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.to2mbn.lolixl.ui.theme.Theme;
import org.to2mbn.lolixl.ui.theme.exception.InvalidThemeException;
import org.to2mbn.lolixl.ui.theme.loading.ThemeLoadingProcessor;
import org.to2mbn.lolixl.ui.theme.loading.ThemeLoadingService;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Logger;

@Component(immediate = true)
@Service({ ThemeLoadingService.class })
public class ThemeLoadingServiceImpl implements ThemeLoadingService {
	private static final Logger LOGGER = Logger.getLogger(ThemeLoadingServiceImpl.class.getCanonicalName());

	private ServiceTracker<ThemeLoadingProcessor, ThemeLoadingProcessor> processorTracker;
	private Queue<ThemeLoadingProcessor> processors;

	@Activate
	public void active(ComponentContext compCtx) {
		processors = new ConcurrentLinkedDeque<>();
		processorTracker = new ServiceTracker<>(compCtx.getBundleContext(), ThemeLoadingProcessor.class, new ServiceTrackerCustomizer<ThemeLoadingProcessor, ThemeLoadingProcessor>() {
			@Override
			public ThemeLoadingProcessor addingService(ServiceReference<ThemeLoadingProcessor> serviceReference) {
				ThemeLoadingProcessor processor = compCtx.getBundleContext().getService(serviceReference);
				registerProcessor(processor);
				return processor;
			}

			@Override
			public void modifiedService(ServiceReference<ThemeLoadingProcessor> serviceReference, ThemeLoadingProcessor themeLoadingProcessor) {}

			@Override
			public void removedService(ServiceReference<ThemeLoadingProcessor> serviceReference, ThemeLoadingProcessor themeLoadingProcessor) {
				unregisterProcessor(themeLoadingProcessor);
			}
		});
		processorTracker.open();
	}

	@Override
	public Optional<Theme> loadAndPublish(URL url) throws IOException, InvalidThemeException {
		Optional<Theme> theme = Optional.empty();
		for (Iterator<ThemeLoadingProcessor> iterator = processors.iterator(); iterator.hasNext(); ) {
			ThemeLoadingProcessor processor = iterator.next();
			if (processor.getChecker().test(url)) {
				LOGGER.info("Loading theme from '" + url.toExternalForm() + "' using '" + processor.getClass().getCanonicalName() + "'");
				theme = Optional.of(processor.process(url));
				// 注册theme为服务
				if (theme.isPresent()) {
					Theme themeObj = theme.get();
					if (themeObj.getId() == null || themeObj.getId().isEmpty()) {
						throw new InvalidThemeException("the publishing theme does not have a ID property!");
					}
					FrameworkUtil.getBundle(getClass()).getBundleContext().registerService(Theme.class, themeObj, new Hashtable<>(Collections.singletonMap(Theme.PROPERTY_KEY_ID, themeObj.getId())));
				}
			}
		}
		return theme;
	}

	private void registerProcessor(ThemeLoadingProcessor processor) {
		LOGGER.info("Registering processor '" + processor.getClass().getCanonicalName() + "'");
		processors.offer(processor);
	}

	private void unregisterProcessor(ThemeLoadingProcessor processor) {
		LOGGER.info("Removing processor '" + processor.getClass().getCanonicalName() + "'");
		processors.remove(processor);
	}
}
