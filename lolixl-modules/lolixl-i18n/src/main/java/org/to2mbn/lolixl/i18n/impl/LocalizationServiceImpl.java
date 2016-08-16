package org.to2mbn.lolixl.i18n.impl;

import static org.to2mbn.lolixl.utils.FXUtils.checkFxThread;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.i18n.LocalizationService;
import org.to2mbn.lolixl.i18n.spi.LocalizationProvider;
import org.to2mbn.lolixl.utils.ObservableServiceTracker;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

@Component(immediate = true)
@Service({ LocalizationService.class })
public class LocalizationServiceImpl implements LocalizationService {

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

	private class LocaleProperty extends SimpleObjectProperty<Locale> {

		public LocaleProperty() {
			super(Locale.getDefault());
		}

		@Override
		public void fireValueChangedEvent() {
			super.fireValueChangedEvent();
		}

	}

	private LocaleProperty currentLocale = new LocaleProperty();

	private Control control = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_PROPERTIES);
	private BundleContext bundleContext;

	private Map<LocalizationCacheKey, Optional<String>> caches = new ConcurrentHashMap<>();
	private ObservableServiceTracker<LocalizationProvider> serviceTracker;

	private ReadWriteLock rwlock = new ReentrantReadWriteLock();

	@Activate
	public void active(ComponentContext compCtx) {
		public_instance = this;
		bundleContext = compCtx.getBundleContext();
		currentLocale.set(Locale.getDefault());
		LOGGER.info("Using locale " + currentLocale);
		serviceTracker = new ObservableServiceTracker<>(bundleContext, LocalizationProvider.class);
		serviceTracker.getServiceList().addListener((Observable dummy) -> refresh());
		serviceTracker.open(true);
	}

	@Deactivate
	public void deactive() {
		public_instance = null;
		serviceTracker.close();
	}

	@Override
	public ObjectProperty<Locale> localeProperty() {
		return currentLocale;
	}

	@Override
	public void refresh() {
		checkFxThread();

		Lock wlock = rwlock.writeLock();
		wlock.lock();
		try {
			caches.clear();
		} finally {
			wlock.unlock();
		}

		currentLocale.fireValueChangedEvent();
	}

	@Override
	public String getLocalizedString(Locale locale, String key) {
		Objects.requireNonNull(locale);
		Objects.requireNonNull(key);
		LocalizationCacheKey cacheKey = new LocalizationCacheKey(locale, key);

		Optional<String> cache;

		cache = caches.get(cacheKey);
		if (cache == null) {
			Lock rlock = rwlock.readLock();

			rlock.lock();
			try {
				cache = lookupLocalizedString(locale, key);
				caches.put(cacheKey, cache);
			} finally {
				rlock.unlock();
			}
		}

		return cache.orElse(null);
	}

	private Optional<String> lookupLocalizedString(Locale idealLocale, String key) {
		List<Locale> locales = control.getCandidateLocales("org.to2mbn.lolixl.i18n", idealLocale);

		// copy on read
		LocalizationProvider[] providers = serviceTracker.getServiceList().toArray(new LocalizationProvider[0]);

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

}
