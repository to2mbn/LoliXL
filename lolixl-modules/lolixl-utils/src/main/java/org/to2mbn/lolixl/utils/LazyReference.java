package org.to2mbn.lolixl.utils;

import java.util.Objects;

public class LazyReference<T> {
	private final boolean allowNullType;
	private T value;

	public LazyReference() {
		this(false);
	}

	public LazyReference(boolean _allowNullType) {
		allowNullType = _allowNullType;
	}

	public LazyReference(T _value) {
		this();
		value = _value;
	}

	public LazyReference(T _value, boolean _allowNullType) {
		value = _value;
		allowNullType = _allowNullType;
	}

	public void set(T val) {
		if (isInitialized()) {
			throw new IllegalStateException("Can not evaluate a non-null lazy-reference");
		}
		value = allowNullType ? val : Objects.requireNonNull(val);
	}

	public T get() {
		if (!isInitialized()) {
			throw new IllegalStateException("Current lazy-reference has not been initialized yet");
		}
		return value;
	}

	public <U extends T> T orElse(U elseVal) {
		if (!isInitialized()) {
			return elseVal;
		}
		return value;
	}

	public boolean isInitialized() {
		return value != null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LazyReference<?> that = (LazyReference<?>) o;
		return value != null ? value.equals(that.value) : that.value == null;
	}

	@Override
	public int hashCode() {
		return value != null ? value.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "LazyReference{" +
				"value=" + value +
				'}';
	}
}
