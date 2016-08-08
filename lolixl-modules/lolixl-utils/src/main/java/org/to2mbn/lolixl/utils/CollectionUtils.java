package org.to2mbn.lolixl.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import javafx.beans.Observable;
import javafx.collections.ObservableList;

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

	public static <T> void bindList(ObservableList<T> src, ObservableList<? super T> dest) {
		src.addListener((Observable dummy) -> dest.setAll(src));
	}

}
