package org.to2mbn.lolixl.i18n.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle.Control;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.osgi.framework.Bundle;

public class ResourceLocalizationProvider implements LocalizationProvider {

	private static final Logger LOGGER = Logger.getLogger(ResourceLocalizationProvider.class.getCanonicalName());

	private Bundle bundle;
	private String baseName;

	private Map<Locale, Map<Object, Object>> caches = new ConcurrentHashMap<>();
	private Control control = Control.getControl(Control.FORMAT_PROPERTIES);

	public ResourceLocalizationProvider(Bundle bundle, String baseName) {
		this.bundle = bundle;
		this.baseName = baseName;
	}

	@Override
	public String getLocalizedString(Locale locale, String key) {
		Map<Object, Object> cache = caches.get(locale);
		if (cache == null) {
			cache = load(locale);
			caches.put(locale, cache);
		}

		Object result = cache.get(key);
		return result == null ? null : result.toString();
	}

	private Map<Object, Object> load(Locale locale) {
		String path = control.toResourceName(control.toBundleName(baseName, locale), "properties");
		URL resource = bundle.getResource(path);
		if (resource == null) {
			return Collections.emptyMap();
		} else {
			try {
				return readProperties(resource);
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, "Couldn't load " + resource, e);
				return Collections.emptyMap();
			}
		}
	}

	private Properties readProperties(URL url) throws IOException {
		Properties properties = new Properties();
		try (InputStream in = url.openStream()) {
				properties.load(new InputStreamReader(in, "UTF-8"));
		}
		return properties;
	}

}
