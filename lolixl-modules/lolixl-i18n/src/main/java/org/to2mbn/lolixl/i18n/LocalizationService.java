package org.to2mbn.lolixl.i18n;

import java.util.Locale;
import javafx.beans.property.ObjectProperty;

public interface LocalizationService {

	ObjectProperty<Locale> localeProperty();

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
