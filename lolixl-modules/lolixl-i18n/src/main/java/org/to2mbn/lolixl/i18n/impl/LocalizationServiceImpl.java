package org.to2mbn.lolixl.i18n.impl;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.to2mbn.lolixl.i18n.LocaleChangedEvent;
import org.to2mbn.lolixl.i18n.LocalizationService;
import org.to2mbn.lolixl.i18n.spi.LocalizationProvider;

@Component(immediate = true)
@Service({ LocalizationService.class })
public class LocalizationServiceImpl implements LocalizationService, ServiceTrackerCustomizer<LocalizationProvider, LocalizationProvider> {

	public static volatile LocalizationService public_instance;

	private static class LocalizationCacheKey {

		public final Locale locale;
		public final String key;

		public LocalizationCacheKey(Locale locale, String key) {
			this.locale = locale;
			this.key = key;
		}

		@Override
		public int hashCode() {
			return locale.hashCode() ^ key.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj instanceof LocalizationCacheKey) {
				LocalizationCacheKey another = (LocalizationCacheKey) obj;
				return key.equals(another.key)
						&& locale.equals(another.locale);
			}
			return false;
		}

	}

	private static final Logger LOGGER = Logger.getLogger(LocalizationServiceImpl.class.getCanonicalName());

	@Reference
	private EventAdmin eventAdmin;

	@Property
	private volatile Locale currentLocale;

	private Control control = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_PROPERTIES);
	private BundleContext bundleContext;

	private Map<LocalizationCacheKey, Optional<String>> caches = new ConcurrentHashMap<>();
	private ServiceTracker<LocalizationProvider, LocalizationProvider> tracker;

	@Activate
	public void active(ComponentContext compCtx) {
		public_instance = this;
		bundleContext = compCtx.getBundleContext();
		currentLocale = Locale.getDefault();
		LOGGER.info("Using locale " + currentLocale);
		tracker = new ServiceTracker<>(bundleContext, LocalizationProvider.class, this);
		tracker.open(true);
	}

	@Deactivate
	public void deactive() {
		public_instance = null;
		tracker.close();
	}

	@Override
	public Locale getCurrentLocale() {
		return currentLocale;
	}

	@Override
	public void setCurrentLocale(Locale locale) {
		Objects.requireNonNull(locale);
		LOGGER.info("Changing locale to " + locale);
		currentLocale = locale;
		eventAdmin.postEvent(new LocaleChangedEvent(locale));
	}

	@Override
	public void refresh() {
		LOGGER.info("Refreshing locale: " + currentLocale);
		eventAdmin.postEvent(new LocaleChangedEvent(currentLocale));
	}

	@Override
	public String getLocalizedString(Locale locale, String key) {
		Objects.requireNonNull(locale);
		Objects.requireNonNull(key);
		LocalizationCacheKey cacheKey = new LocalizationCacheKey(locale, key);

		Optional<String> cache;

		cache = caches.get(cacheKey);
		if (cache == null) {
			cache = lookupLocalizedString(locale, key);
			caches.put(cacheKey, cache);
		}

		return cache.orElse(null);
	}

	private Optional<String> lookupLocalizedString(Locale idealLocale, String key) {
		List<Locale> locales = control.getCandidateLocales("org.to2mbn.lolixl.i18n", idealLocale);
		for (Locale locale : locales) {
			for (LocalizationProvider provider : tracker.getServices(new LocalizationProvider[0])) {
				String result = provider.getLocalizedString(locale, key);
				if (result != null) {
					return Optional.of(result);
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public LocalizationProvider addingService(ServiceReference<LocalizationProvider> reference) {
		clearCache();
		return bundleContext.getService(reference);
	}

	@Override
	public void modifiedService(ServiceReference<LocalizationProvider> reference, LocalizationProvider service) {
		clearCache();
	}

	@Override
	public void removedService(ServiceReference<LocalizationProvider> reference, LocalizationProvider service) {
		bundleContext.ungetService(reference);
		clearCache();
	}

	private void clearCache() {
		caches.clear();
		refresh();
	}

}
