package org.to2mbn.lolixl.i18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.osgi.service.event.Event;

public class LocaleChangedEvent extends Event {

	public static final String TOPIC_LOCALE_CHANGED = "org.to2mbn.lolixl.i18n.localeChanged";
	public static final String KEY_NEW_LOCALE = "org.to2mbn.lolixl.i18n.newLocale";

	private static Map<String, Object> createProperties(Locale newLocale) {
		Map<String, Object> properties = new HashMap<>();
		properties.put(KEY_NEW_LOCALE, newLocale);
		return properties;
	}

	private Locale newLocale;

	public LocaleChangedEvent(Locale newLocale) {
		super(TOPIC_LOCALE_CHANGED, createProperties(newLocale));
		this.newLocale = newLocale;
	}

	public Locale getNewLocale() {
		return newLocale;
	}

}
