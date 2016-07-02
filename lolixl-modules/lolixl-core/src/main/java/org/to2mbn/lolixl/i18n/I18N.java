package org.to2mbn.lolixl.i18n;

import java.util.Arrays;
import java.util.Locale;
import org.to2mbn.lolixl.i18n.impl.I18NActivator;

public final class I18N {

	private I18N() {}

	public String localize(String key, Object... arguments) {
		return localize(getLocalizationService().getCurrentLocale(), key, arguments);
	}

	public String localize(Locale locale, String key, Object... arguments) {
		String result = getLocalizationService().getLocalizedString(locale, key);
		if (result != null) {
			return String.format(result, arguments);
		} else {
			return key + Arrays.toString(arguments);
		}
	}

	private static LocalizationService getLocalizationService() {
		if (I18NActivator.publicService == null) {
			throw new IllegalStateException("No LocalizationService is available");
		}
		return I18NActivator.publicService;
	}
}
