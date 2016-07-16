package org.to2mbn.lolixl.utils;

public interface Storable<MEMO extends java.io.Serializable> {

	MEMO store();

	void restore(MEMO memento);

	Class<? extends MEMO> getMementoType();

}
