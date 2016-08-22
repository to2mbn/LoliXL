package org.to2mbn.lolixl.ui.impl.theme;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.to2mbn.lolixl.plugin.PluginManager;
import org.to2mbn.lolixl.plugin.util.PluginResourceListener;
import org.to2mbn.lolixl.ui.impl.MainScene;
import org.to2mbn.lolixl.ui.impl.util.CssUtils;
import org.to2mbn.lolixl.utils.ParameterizedTypeUtils;
import javafx.scene.Scene;

@Component
public class PluginCssResolver {

	private static final Logger LOGGER = Logger.getLogger(PluginCssResolver.class.getCanonicalName());

	@Reference(target = "(" + MainScene.PROPERTY_SCENE_ID + "=" + MainScene.MAIN_SCENE_ID + ")")
	private Scene scene;

	@Reference
	private PluginManager pluginManager;

	private PluginResourceListener<Set<String>> resourceListener = PluginResourceListener
			.<Set<String>> json("META-INF/lolixl/css.json", ParameterizedTypeUtils.createParameterizedType(Set.class, String.class))
			.whenAdding((plugin, cssFiles) -> Optional.ofNullable(this.scene).ifPresent(scene -> {
				List<String> css = CssUtils.mapCssToUrls(plugin.getBundle(), cssFiles);
				LOGGER.info("Loading css " + css);
				scene.getStylesheets().addAll(css);
			}))
			.whenRemoving((plugin, cssFiles) -> Optional.ofNullable(this.scene).ifPresent(scene -> {
				List<String> css = CssUtils.mapCssToUrls(plugin.getBundle(), cssFiles);
				LOGGER.info("Unloading css " + css);
				scene.getStylesheets().removeAll(css);
			}));

	@Activate
	public void active() {
		resourceListener.bindTo(pluginManager);
	}
}
