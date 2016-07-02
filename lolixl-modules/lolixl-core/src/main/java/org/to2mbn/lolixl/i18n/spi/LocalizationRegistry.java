package org.to2mbn.lolixl.i18n.spi;

import org.osgi.framework.Bundle;

public interface LocalizationRegistry {

	void register(Bundle bundle, LocalizationProvider provider);

	/**
	 * 注册一组.properties的语言文件。
	 * <p>
	 * 语言文件应存放在bundle jar的根目录下，名称格式为
	 * 
	 * <pre>
	 * &lt;baseName&gt;_&lt;locale&gt;.properties
	 * </pre>
	 * 
	 * 例如
	 * 
	 * <pre>
	 * org.to2mbn.lolixl_en_US.properties
	 * org.to2mbn.lolixl_zh_CN.properties
	 * </pre>
	 * 
	 * 语言文件使用UTF-8编码。
	 * 
	 * @param bundle bundle
	 * @param baseName 语言文件的前缀
	 */
	default void registerProperties(Bundle bundle, String baseName) {
		register(bundle, new ResourceLocalizationProvider(bundle, baseName));
	}

}
