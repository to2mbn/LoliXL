package org.to2mbn.lolixl.i18n.impl;

import static java.lang.String.format;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.to2mbn.lolixl.i18n.spi.LocalizationProvider;
import org.to2mbn.lolixl.i18n.spi.ResourceLocalizationProvider;
import org.to2mbn.lolixl.plugin.PluginManager;
import org.to2mbn.lolixl.plugin.util.PluginResourceListener;
import org.to2mbn.lolixl.utils.ParameterizedTypeUtils;

@Component
public class PluginLanguageFilesResolver {

	private static final Logger LOGGER = Logger.getLogger(PluginLanguageFilesResolver.class.getCanonicalName());

	@Reference
	private PluginManager pluginManager;

	private PluginResourceListener<Set<String>> resourceListener = PluginResourceListener
			.<Set<String>> json("META-INF/lolixl/lang.json", ParameterizedTypeUtils.createParameterizedType(Set.class, String.class))
			.whenAdding((plugin, languageFiles) -> {
				Bundle bundle = plugin.getBundle();
				BundleContext ctx = bundle.getBundleContext();

				for (String lang : languageFiles) {
					LOGGER.info(format("Loading language properties [%s] from plugin [%s]", lang, plugin.getDescription().getArtifact()));
					ctx.registerService(LocalizationProvider.class, new ResourceLocalizationProvider(bundle, lang), null);
				}
			});

	@Activate
	public void active() {
		resourceListener.bindTo(pluginManager);
	}

}
