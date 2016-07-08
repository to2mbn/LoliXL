package org.to2mbn.lolixl.utils.internal;

import org.apache.felix.scr.annotations.Component;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component
public class GsonFactory implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		context.registerService(Gson.class,
				new GsonBuilder()
						.setPrettyPrinting()
						.create(),
				null);
	}

	@Override
	public void stop(BundleContext context) throws Exception {}

}
