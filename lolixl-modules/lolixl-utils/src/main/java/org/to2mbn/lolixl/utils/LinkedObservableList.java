package org.to2mbn.lolixl.utils;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.ListBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LinkedObservableList<E> extends ListBinding<E> {

	private ObservableList<E>[] links;

	@SafeVarargs
	public LinkedObservableList(ObservableList<E>... links) {
		this.links = links;
		bind(links);
	}

	@Override
	protected ObservableList<E> computeValue() {
		List<E> list = new ArrayList<>();
		for (ObservableList<E> link : links)
			list.addAll(link);
		return FXCollections.unmodifiableObservableList(FXCollections.observableList(list));
	}


}
