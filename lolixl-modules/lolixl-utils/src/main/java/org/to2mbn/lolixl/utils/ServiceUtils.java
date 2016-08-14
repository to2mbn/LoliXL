package org.to2mbn.lolixl.utils;

import static java.lang.String.format;
import java.util.function.Function;
import java.util.logging.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public final class ServiceUtils {

	private static final Logger LOGGER = Logger.getLogger(ServiceUtils.class.getCanonicalName());

	private ServiceUtils() {}

	public static <T> String getIdProperty(String property, ServiceReference<T> reference, T service) {
		Object propVal = reference.getProperty(property);
		if (propVal instanceof String) {
			return (String) propVal;
		} else if (propVal == null) {
			LOGGER.warning(format("Property %s of %s is not found", property, service.getClass().getName()));
		} else {
			// illegal property type
			LOGGER.warning(format("Illegal property type %s of %s: expected %s, actual %s", property, service.getClass().getName(), String.class.getName(), propVal.getClass().getName()));
		}
		// fallback
		return service.getClass().getName().replace('$', '.');
	}

	public static <T, S> T doWithService(Class<S> service, Class<?> serviceUser, Function<S, T> action) {
		BundleContext ctx = FrameworkUtil.getBundle(serviceUser).getBundleContext();
		ServiceReference<S> ref = ctx.getServiceReference(service);
		if (ref == null) {
			throw new IllegalStateException(format("Service %s is not available", service.getName()));
		}
		S s = ctx.getService(ref);
		try {
			return action.apply(s);
		} finally {
			ctx.ungetService(ref);
		}
	}

}
