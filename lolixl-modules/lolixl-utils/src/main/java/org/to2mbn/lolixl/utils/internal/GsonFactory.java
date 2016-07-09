package org.to2mbn.lolixl.utils.internal;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.osgi.service.component.ComponentContext;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component
public class GsonFactory {

	@Activate
	public void activate(ComponentContext compCtx) {
		compCtx.getBundleContext().registerService(Gson.class,
				new GsonBuilder()
						.setPrettyPrinting()
						.create(),
				null);
	}


}
