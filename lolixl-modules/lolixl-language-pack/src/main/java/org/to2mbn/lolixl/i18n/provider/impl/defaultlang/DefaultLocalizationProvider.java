package org.to2mbn.lolixl.i18n.provider.impl.defaultlang;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.i18n.spi.LocalizationProvider;
import org.to2mbn.lolixl.i18n.spi.ResourceLocalizationProvider;

@Component
public class DefaultLocalizationProvider {

	@Activate
	public void active(ComponentContext compCtx) {
		BundleContext ctx = compCtx.getBundleContext();
		ctx.registerService(LocalizationProvider.class, new ResourceLocalizationProvider(ctx.getBundle(), "org.to2mbn.lolixl.i18n.provider.impl.defaultlang"), null);
	}

}
