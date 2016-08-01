package org.to2mbn.lolixl.utils.internal;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.osgi.service.component.ComponentContext;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.beans.property.Property;

@Component
public class GsonFactory {

	public static Gson instance;

	@Activate
	public void activate(ComponentContext compCtx) {
		instance = new GsonBuilder()
				.registerTypeAdapter(Property.class, new PropertyTypeAdapter())
				.setPrettyPrinting()
				.create();
		compCtx.getBundleContext().registerService(Gson.class, instance, null);
	}

	@Deactivate
	public void deactive() {
		instance = null;
	}

}
