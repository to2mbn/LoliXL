package org.to2mbn.lolixl.utils;

import java.util.Optional;

public interface Storable<MEMO extends java.io.Serializable> {

	MEMO store();

	/**
	 * @param memento 备忘录，可能不存在。若不存在，则调用该方法仅仅是表明上下文已经尝试加载备忘录，但备忘录不存在
	 */
	void restore(Optional<MEMO> memento);

	Class<? extends MEMO> getMementoType();

}
