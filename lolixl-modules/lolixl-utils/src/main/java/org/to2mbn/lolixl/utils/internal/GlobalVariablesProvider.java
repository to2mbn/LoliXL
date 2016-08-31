package org.to2mbn.lolixl.utils.internal;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.utils.DictionaryAdapter;
import org.to2mbn.lolixl.utils.GlobalVariables;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;

@Component
public class GlobalVariablesProvider {

	private List<ServiceRegistration<?>> registrations = new ArrayList<>();
	private BundleContext ctx;

	@SuppressWarnings("unchecked")
	@Activate
	public void active(ComponentContext compCtx) {
		ctx = compCtx.getBundleContext();

		register(GlobalVariables.VALUE_ANIMATION_TIME_MULTIPLIER, new SimpleDoubleProperty(1.0), ObservableDoubleValue.class, DoubleProperty.class);
	}

	private <T> void register(String type, T obj, @SuppressWarnings("unchecked") Class<? super T>... services) {
		Map<String, Object> properties = new HashMap<>();
		properties.put(GlobalVariables.PROPERTY_GLOBAL_VARIABLE, type);
		Dictionary<String, Object> adaptedProperties = new DictionaryAdapter<>(properties);
		for (Class<? super T> service : services) {
			registrations.add(ctx.registerService(service, obj, adaptedProperties));
		}
	}

	@Deactivate
	public void deactive(ComponentContext compCtx) {
		registrations.forEach(reg -> reg.unregister());
		registrations.clear();
	}

}
