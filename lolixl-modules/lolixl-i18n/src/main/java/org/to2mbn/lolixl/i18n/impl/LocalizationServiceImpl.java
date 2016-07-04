package org.to2mbn.lolixl.i18n.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.ResourceBundle.Control;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.event.EventAdmin;
import org.to2mbn.lolixl.i18n.LocaleChangedEvent;
import org.to2mbn.lolixl.i18n.LocalizationService;
import org.to2mbn.lolixl.i18n.spi.LocalizationProvider;
import org.to2mbn.lolixl.i18n.spi.LocalizationRegistry;

@Component
@Service({ LocalizationService.class, LocalizationRegistry.class })
public class LocalizationServiceImpl implements LocalizationService, LocalizationRegistry, BundleListener {

	public static LocalizationService public_instance;

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

	private Locale currentLocale;
	private Control control = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_PROPERTIES);

	private Set<LocalizationProvider> providers = new CopyOnWriteArraySet<>();
	private Map<Bundle, Set<LocalizationProvider>> bundleMapping = new HashMap<>();
	private Map<LocalizationCacheKey, Optional<String>> caches = new ConcurrentHashMap<>();
	private ReadWriteLock providersLock = new ReentrantReadWriteLock();

	public LocalizationServiceImpl() {
		public_instance = this;
		currentLocale = Locale.getDefault();
		LOGGER.info("Using locale " + currentLocale);
	}

	@Modified
	public void updated(Map<String, Object> properties) throws ConfigurationException {
		Locale newLocale = (Locale) properties.get(CONFIG_LOCALE);
		if (newLocale != null) {
			setCurrentLocale(newLocale);
		}
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

		Lock rlock = providersLock.readLock();
		rlock.lock();
		try {
			cache = caches.get(cacheKey);
			if (cache == null) {
				cache = lookupLocalizedString(locale, key);
				caches.put(cacheKey, cache);
			}
		} finally {
			rlock.unlock();
		}

		return cache.orElse(null);
	}

	@Override
	public void register(Bundle bundle, LocalizationProvider provider) {
		Objects.requireNonNull(bundle);
		Objects.requireNonNull(provider);
		Lock wlock = providersLock.writeLock();
		wlock.lock();
		try {
			Set<LocalizationProvider> bundleProviders = bundleMapping.get(bundle);
			if (bundleProviders == null) {
				bundleProviders = new HashSet<>();
				bundleMapping.put(bundle, bundleProviders);
			}
			bundleProviders.add(provider);
			providers.add(provider);

			if (bundle.getState() != Bundle.ACTIVE) {
				removeBundleProviders(bundle);
			}

			caches.clear();
		} finally {
			wlock.unlock();
		}

		refresh();
	}

	public void checkBundleState(Bundle bundle) {
		if (bundle.getState() != Bundle.ACTIVE) {
			Lock wlock = providersLock.writeLock();
			wlock.lock();
			try {
				if (!removeBundleProviders(bundle)) {
					return;
				}
				caches.clear();
			} finally {
				wlock.unlock();
			}

			refresh();
		}
	}

	/**
	 * @param bundle bundle
	 * @return 该bundle是否注册有LocalizationProvider
	 */
	private boolean removeBundleProviders(Bundle bundle) {
		Set<LocalizationProvider> bundleProviders = bundleMapping.remove(bundle);
		if (bundleProviders != null) {
			providers.removeAll(bundleProviders);
			return true;
		}
		return false;
	}

	private Optional<String> lookupLocalizedString(Locale idealLocale, String key) {
		List<Locale> locales = control.getCandidateLocales("org.to2mbn.lolixl.i18n", idealLocale);
		for (Locale locale : locales) {
			for (LocalizationProvider provider : providers) {
				String result = provider.getLocalizedString(locale, key);
				if (result != null) {
					return Optional.of(result);
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public void bundleChanged(BundleEvent event) {
		checkBundleState(event.getBundle());
	}

}
