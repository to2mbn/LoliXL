package org.to2mbn.lolixl.utils;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;

public class DictionaryAdapter<K, V> extends Dictionary<K, V> {

	private Map<K, V> adapted;

	public DictionaryAdapter(Map<K, V> adapted) {
		this.adapted = Objects.requireNonNull(adapted);
	}

	@Override
	public int size() {
		return adapted.size();
	}

	@Override
	public boolean isEmpty() {
		return adapted.isEmpty();
	}

	@Override
	public Enumeration<K> keys() {
		return Collections.enumeration(adapted.keySet());
	}

	@Override
	public Enumeration<V> elements() {
		return Collections.enumeration(adapted.values());
	}

	@Override
	public V get(Object key) {
		return adapted.get(key);
	}

	@Override
	public V put(K key, V value) {
		return adapted.put(key, value);
	}

	@Override
	public V remove(Object key) {
		return adapted.remove(key);
	}

}
