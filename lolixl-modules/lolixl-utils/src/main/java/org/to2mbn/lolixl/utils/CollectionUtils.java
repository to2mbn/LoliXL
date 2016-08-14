package org.to2mbn.lolixl.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;

public final class CollectionUtils {

	private CollectionUtils() {}

	public static <T> void diff(Collection<T> from, Collection<T> to, Consumer<? super T> foreachAdd, Consumer<? super T> foreachRemove) {
		List<T> added = new ArrayList<>(to);
		added.removeAll(from);
		added.forEach(foreachAdd);

		List<T> removed = new ArrayList<>(from);
		removed.removeAll(to);
		removed.forEach(foreachRemove);
	}

	public static <T, C extends Collection<T> & Observable> InvalidationListener addDiffListener(C collection, Consumer<? super T> foreachAdd, Consumer<? super T> foreachRemove) {
		CollectionDiffListener<T> differ = new CollectionDiffListener<>();
		differ.target = collection;
		differ.foreachAdd = foreachAdd;
		differ.foreachRemove = foreachRemove;
		collection.addListener(new WeakInvalidationListener(differ));
		differ.invalidated(collection);
		return differ;
	}

	private static class CollectionDiffListener<T> implements InvalidationListener {

		Collection<T> target;
		Consumer<? super T> foreachAdd;
		Consumer<? super T> foreachRemove;

		Collection<T> lastSnapshot = Collections.emptyList();
		Object lock = new Object();

		@Override
		public void invalidated(Observable observable) {
			synchronized (lock) {
				diff(lastSnapshot, target, foreachAdd, foreachRemove);
				lastSnapshot = new ArrayList<>(target);
			}
		}

	}

}
