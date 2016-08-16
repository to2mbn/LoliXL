package org.to2mbn.lolixl.utils;

import static java.util.stream.Collectors.*;
import java.util.function.Function;
import java.util.stream.Stream;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ObservableServiceTracker<T> extends LambdaServiceTracker<T> {

	private ObservableList<T> tracked = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
	private ObservableList<T> trackedView = FXCollections.unmodifiableObservableList(tracked);

	private Function<Stream<ServiceReference<T>>, Stream<T>> mapper;

	public ObservableServiceTracker(BundleContext context, Class<T> clazz) {
		this(context, clazz, null);
	}

	public ObservableServiceTracker(BundleContext context, Class<T> clazz, Function<Stream<ServiceReference<T>>, Stream<T>> mapper) {
		super(context, clazz);
		this.mapper = mapper == null
				? stream -> stream
						.map(this::getService)
				: mapper;
	}

	@Override
	public T addingService(ServiceReference<T> reference) {
		T service = super.addingService(reference);
		updateTrackedList();
		return service;
	}

	@Override
	public void modifiedService(ServiceReference<T> reference, T service) {
		super.modifiedService(reference, service);
		updateTrackedList();
	}

	@Override
	public void removedService(ServiceReference<T> reference, T service) {
		super.removedService(reference, service);
		updateTrackedList();
	}

	public void updateTrackedList() {
		Platform.runLater(() -> {
			ServiceReference<T>[] refs = getServiceReferences();
			if (refs == null) {
				// the service tracker has been closed
				return;
			}
			tracked.setAll(
					mapper.apply(Stream.of(refs))
							.collect(toList()));
		});
	}

	public ObservableList<T> getServiceList() {
		return trackedView;
	}

}
