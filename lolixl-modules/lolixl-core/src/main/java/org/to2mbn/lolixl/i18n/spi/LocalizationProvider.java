package org.to2mbn.lolixl.i18n.spi;

import java.util.Locale;

public interface LocalizationProvider {

	/**
	 * @param locale 语言环境
	 * @param key 文本的key
	 * @return 对应的经过本地化的文本，可能为null
	 */
	String getLocalizedString(Locale locale, String key);

}
