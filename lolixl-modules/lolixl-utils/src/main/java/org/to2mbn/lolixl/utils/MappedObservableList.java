package org.to2mbn.lolixl.utils;

import static java.util.stream.Collectors.*;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import javafx.beans.binding.ListBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MappedObservableList<SRC, DEST> extends ListBinding<DEST> {

	private ObservableList<SRC> src;
	private Function<SRC, DEST> mapper;

	private Map<SRC, DEST> mapping = new ConcurrentHashMap<>();
	private Map<DEST, SRC> mappingR = new ConcurrentHashMap<>();

	private Map<SRC, DEST> mappingView = Collections.unmodifiableMap(mapping);
	private Map<DEST, SRC> mappingRView = Collections.unmodifiableMap(mappingR);

	public MappedObservableList(ObservableList<SRC> src, Function<SRC, DEST> mapper) {
		this.src = src;
		this.mapper = mapper;
		bind(src);
	}

	@Override
	protected ObservableList<DEST> computeValue() {
		synchronized (mapping) {
			CollectionUtils.diff(mapping.keySet(), src,
					added -> {
						DEST mapped = mapper.apply(added);
						mapping.put(added, mapped);
						mappingR.put(mapped, added);
					},
					removed -> mappingR.remove(mapping.remove(removed)));
			return FXCollections.unmodifiableObservableList(
					FXCollections.observableList(
							src.stream()
									.map(mapper)
									.collect(toList())));
		}
	}

	public Map<SRC, DEST> mapping() {
		return mappingView;
	}

	public Map<DEST, SRC> inverseMapping() {
		return mappingRView;
	}

}
