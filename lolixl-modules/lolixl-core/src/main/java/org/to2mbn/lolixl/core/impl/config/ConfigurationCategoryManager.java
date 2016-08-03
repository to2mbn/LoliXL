package org.to2mbn.lolixl.core.impl.config;

import static java.lang.String.format;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.to2mbn.lolixl.core.config.Configuration;
import org.to2mbn.lolixl.core.config.ConfigurationCategory;
import org.to2mbn.lolixl.core.config.ConfigurationEvent;
import org.to2mbn.lolixl.core.config.ConfigurationManager;
import org.to2mbn.lolixl.utils.GsonUtils;
import org.to2mbn.lolixl.utils.ObservableContext;
import org.to2mbn.lolixl.utils.ServiceUtils;

@SuppressWarnings({ "rawtypes", "unchecked" })

@Service({ ConfigurationManager.class })
@Component(immediate = true)
public class ConfigurationCategoryManager implements ConfigurationManager {

	private static final Logger LOGGER = Logger.getLogger(ConfigurationCategoryManager.class.getCanonicalName());

	@Reference(target = "(usage=local_io)")
	private ExecutorService localIOPool;

	@Reference
	private EventAdmin eventAdmin;

	private Path storeLocation = Paths.get(".lolixl", "config");

	private BundleContext bundleContext;
	private ServiceTracker<ConfigurationCategory, ConfigurationCategory> serviceTracker;

	@Activate
	public void active(ComponentContext compCtx) throws InvalidSyntaxException {
		bundleContext = compCtx.getBundleContext();
		serviceTracker = new ServiceTracker<>(bundleContext, ConfigurationCategory.class, new ServiceTrackerCustomizer<ConfigurationCategory, ConfigurationCategory>() {

			@Override
			public void modifiedService(ServiceReference<ConfigurationCategory> reference, ConfigurationCategory service) {}

			@Override
			public void removedService(ServiceReference<ConfigurationCategory> reference, ConfigurationCategory service) {
				bundleContext.ungetService(reference);
			}

			@Override
			public ConfigurationCategory<?> addingService(ServiceReference<ConfigurationCategory> reference) {
				ConfigurationCategory service = bundleContext.getService(reference);
				Configuration configuration = tryLoadConfiguration(reference, service);

				ObservableContext observableContext = new ObservableContext();
				observableContext.addListener(dummy -> {
					updateConfiguration(reference, service);
					localIOPool.submit(() -> trySaveConfiguration(reference, service));
				});
				service.setObservableContext(observableContext);

				try {
					service.restore(Optional.ofNullable(configuration));
				} catch (Exception e) {
					LOGGER.log(Level.WARNING, format("Couldn't restore configuration [%s] for [%s]", configuration, service), e);
				}

				updateConfiguration(reference, service);

				return service;
			}

		});
		serviceTracker.open(true);
	}

	@Deactivate
	public void deactive() {
		serviceTracker.close();
	}

	private Configuration tryLoadConfiguration(ServiceReference<ConfigurationCategory> reference, ConfigurationCategory<?> service) {
		try {
			Path location = getConfigurationPath(reference, service);
			if (!Files.isRegularFile(location)) {
				LOGGER.fine(format("No configuration found at [%s]", location));
				return null;
			}

			Class<? extends Configuration> clazz = service.getMementoType();
			LOGGER.fine(format("Loading configuration from [%s], class=[%s]", location, clazz));
			return GsonUtils.fromJson(location, clazz);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, format("Couldn't read configuration for [%s]", service), e);
			return null;
		}
	}

	private void trySaveConfiguration(ServiceReference<ConfigurationCategory> reference, ConfigurationCategory<?> service) {
		try {
			Path location = getConfigurationPath(reference, service);

			LOGGER.fine(format("Storing configuration to [%s]", location));
			synchronized (service) {
				GsonUtils.toJson(location, service.store());
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, format("Couldn't save configuration for [%s]", service), e);
		}
	}

	private String getCategoryName(ServiceReference<ConfigurationCategory> reference, ConfigurationCategory<?> service) {
		return ServiceUtils.getIdProperty(ConfigurationCategory.PROPERTY_CATEGORY, reference, service);
	}

	private Path getConfigurationPath(ServiceReference<ConfigurationCategory> reference, ConfigurationCategory<?> service) {
		return storeLocation.resolve(getCategoryName(reference, service) + ".json");
	}

	private void updateConfiguration(ServiceReference<ConfigurationCategory> reference, ConfigurationCategory<?> service) {
		try {
			Configuration configuration = service.store();
			String category = getCategoryName(reference, service);
			Event event = new ConfigurationEvent(configuration, category);
			eventAdmin.postEvent(event);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, format("Couldn't post configuration update event for %s", reference), e);
		}
	}

	@Override
	public Optional<ConfigurationCategory<?>> getCategory(String category) {
		Objects.requireNonNull(category);
		for (ServiceReference<ConfigurationCategory> reference : serviceTracker.getServiceReferences()) {
			ConfigurationCategory<?> service = bundleContext.getService(reference);
			try {
				if (category.equals(getCategoryName(reference, service))) {
					return Optional.of(service);
				}
			} finally {
				bundleContext.ungetService(reference);
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<Configuration> getConfiguration(String category) {
		return getCategory(category).map(ConfigurationCategory::store);
	}

}
