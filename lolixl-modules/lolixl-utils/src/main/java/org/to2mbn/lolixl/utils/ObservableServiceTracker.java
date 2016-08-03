package org.to2mbn.lolixl.utils;

import java.lang.reflect.Array;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ObservableServiceTracker<T> extends ServiceTracker<T, T> {

	private ObservableList<T> tracked = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
	private ObservableList<T> trackedView = FXCollections.unmodifiableObservableList(tracked);

	private T[] zeroLengthArray;

	@SuppressWarnings("unchecked")
	public ObservableServiceTracker(BundleContext context, Class<T> clazz) {
		super(context, clazz, null);
		zeroLengthArray = (T[]) Array.newInstance(clazz, 0);
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

	private void updateTrackedList() {
		tracked.setAll(getServices(zeroLengthArray));
	}

	public ObservableList<T> getServiceList() {
		return trackedView;
	}

}
