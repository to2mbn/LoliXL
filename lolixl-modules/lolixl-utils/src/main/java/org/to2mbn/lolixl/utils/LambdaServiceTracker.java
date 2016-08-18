package org.to2mbn.lolixl.utils;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import javafx.application.Platform;

public class LambdaServiceTracker<T> extends ServiceTracker<T, T> {

	private static final Logger LOGGER = Logger.getLogger(LambdaServiceTracker.class.getCanonicalName());

	private List<BiConsumer<ServiceReference<T>, T>> addActions = new Vector<>();
	private List<BiConsumer<ServiceReference<T>, T>> modifyActions = new Vector<>();
	private List<BiConsumer<ServiceReference<T>, T>> removeActions = new Vector<>();
	private Map<T, ServiceReference<T>> serviceRefMapping = new ConcurrentHashMap<>();

	public LambdaServiceTracker(BundleContext context, Class<T> clazz) {
		super(context, clazz, null);
	}

	public LambdaServiceTracker(BundleContext context, Filter filter) {
		super(context, filter, null);
	}

	public LambdaServiceTracker(BundleContext context, String clazz) {
		super(context, clazz, null);
	}

	public LambdaServiceTracker<T> whenAdding(BiConsumer<ServiceReference<T>, T> action) {
		addActions.add(action);
		return this;
	}

	public LambdaServiceTracker<T> whenModifying(BiConsumer<ServiceReference<T>, T> action) {
		modifyActions.add(action);
		return this;
	}

	public LambdaServiceTracker<T> whenRemoving(BiConsumer<ServiceReference<T>, T> action) {
		removeActions.add(action);
		return this;
	}

	@Override
	public T addingService(ServiceReference<T> reference) {
		T service = super.addingService(reference);
		if (service != null) {
			serviceRefMapping.put(service, reference);
			invoke(addActions, reference, service);
		}
		return service;
	}

	@Override
	public void modifiedService(ServiceReference<T> reference, T service) {
		super.modifiedService(reference, service);
		if (service != null) {
			invoke(modifyActions, reference, service);
		}
	}

	@Override
	public void removedService(ServiceReference<T> reference, T service) {
		invoke(removeActions, reference, service);
		if (service != null) {
			super.removedService(reference, service);
			serviceRefMapping.remove(service);
		}
	}

	public ServiceReference<T> getServiceReference(T service) {
		return serviceRefMapping.get(service);
	}

	private void invoke(List<BiConsumer<ServiceReference<T>, T>> listeners, ServiceReference<T> reference, T service) {
		Platform.runLater(() -> {
			for (BiConsumer<ServiceReference<T>, T> listener : listeners) {
				try {
					listener.accept(reference, service);
				} catch (Throwable e) {
					LOGGER.log(Level.WARNING, "Uncaught exception from service tracker listener " + listener, e);
				}
			}
		});
	}

}
