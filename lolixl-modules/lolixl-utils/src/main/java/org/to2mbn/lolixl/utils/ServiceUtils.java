package org.to2mbn.lolixl.utils;

import static java.lang.String.format;
import java.util.logging.Logger;
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

}
