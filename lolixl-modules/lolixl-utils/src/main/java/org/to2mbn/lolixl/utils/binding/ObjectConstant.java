package org.to2mbn.lolixl.utils.binding;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableObjectValue;

class ObjectConstant<T> implements ObservableObjectValue<T> {

	private T value;

	public ObjectConstant(T value) {
		this.value = value;
	}

	@Override
	public T get() {
		return value;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public void addListener(InvalidationListener observer) {}

	@Override
	public void addListener(ChangeListener<? super T> observer) {}

	@Override
	public void removeListener(InvalidationListener observer) {}

	@Override
	public void removeListener(ChangeListener<? super T> observer) {}
}
