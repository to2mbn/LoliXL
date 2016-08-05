package org.to2mbn.lolixl.i18n.impl;

import static java.lang.String.format;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.i18n.spi.LocalizationProvider;
import org.to2mbn.lolixl.i18n.spi.ResourceLocalizationProvider;
import org.to2mbn.lolixl.plugin.Plugin;
import org.to2mbn.lolixl.plugin.PluginService;

@Component
public class BundleLanguageFilesResolver implements BundleListener {

	private static final Logger LOGGER = Logger.getLogger(BundleLanguageFilesResolver.class.getCanonicalName());

	@Reference
	private PluginService pluginService;

	@Activate
	public void active(ComponentContext compCtx) {
		pluginService.getLoadedPlugins().forEach(this::tryLoadPluginLanguageFiles);
		compCtx.getBundleContext().addBundleListener(this);
	}

	@Deactivate
	public void deactive(ComponentContext compCtx) {
		compCtx.getBundleContext().removeBundleListener(this);
	}

	@Override
	public void bundleChanged(BundleEvent event) {
		if (event.getType() == BundleEvent.STARTED) {
			Bundle bundle = event.getBundle();
			pluginService.getPlugin(bundle).ifPresent(this::tryLoadPluginLanguageFiles);
		}
	}

	private void tryLoadPluginLanguageFiles(Plugin plugin) {
		Bundle bundle = plugin.getBundle();
		if ((bundle.adapt(BundleRevision.class).getTypes() & BundleRevision.TYPE_FRAGMENT) != 0) {
			return;
		}

		BundleContext ctx;

		// 自旋锁
		do {
			int state = bundle.getState();
			if (state != Bundle.STARTING && state != Bundle.ACTIVE) {
				return;
			}
			Thread.yield();
			ctx = bundle.getBundleContext();
		} while (ctx == null);

		Set<String> languageFiles = plugin.getDescription().getLanguageFiles();
		for (String lang : languageFiles) {
			LOGGER.info(format("Loading language properties [%s] from plugin [%s]", lang, plugin.getDescription().getArtifact()));
			ctx.registerService(LocalizationProvider.class, new ResourceLocalizationProvider(bundle, lang), null);
		}
	}

}
