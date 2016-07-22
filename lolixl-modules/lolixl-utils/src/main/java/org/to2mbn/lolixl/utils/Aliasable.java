package org.to2mbn.lolixl.utils;

public interface Aliasable {

	/**
	 * @return 别名，可能为null
	 */
	String getAlias();

	/**
	 * @param alias 别名，可以为null
	 */
	void setAlias(String alias);

}