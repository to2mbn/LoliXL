package org.to2mbn.lolixl.i18n;

import java.util.Arrays;
import java.util.Locale;
import org.to2mbn.lolixl.i18n.impl.LocalizationServiceImpl;

public final class I18N {

	private I18N() {}

	public static String localize(String key, Object... arguments) {
		return localize(getLocalizationService().getCurrentLocale(), key, arguments);
	}

	public static String localize(Locale locale, String key, Object... arguments) {
		String result = getLocalizationService().getLocalizedString(locale, key);
		if (result != null) {
			return String.format(result, arguments);
		} else {
			return key + Arrays.toString(arguments);
		}
	}

	private static LocalizationService getLocalizationService() {
		LocalizationService service = LocalizationServiceImpl.public_instance;
		if (service == null) {
			throw new IllegalStateException("No LocalizationService is available");
		}
		return service;
	}
}
