package org.to2mbn.lolixl.i18n;

import java.util.Locale;

public interface LocalizationService {

	String CONFIG_LOCALE = "org.to2mbn.lolixl.i18n.locale";


	Locale getCurrentLocale();

	void setCurrentLocale(Locale locale);

	/**
	 * 刷新当前语言环境。
	 */
	void refresh();

	/**
	 * @param locale 语言环境
	 * @param key 文本的key
	 * @return 对应的经过本地化的文本，可能为null
	 */
	String getLocalizedString(Locale locale, String key);

}
