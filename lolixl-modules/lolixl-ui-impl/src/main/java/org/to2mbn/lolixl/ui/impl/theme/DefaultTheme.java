package org.to2mbn.lolixl.ui.impl.theme;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.ui.theme.Theme;
import javafx.scene.image.Image;

@Service({ Theme.class })
@Properties({
		@Property(name = Theme.PROPERTY_THEME_ID, value = "org.to2mbn.lolixl.ui.impl.theme.default")
})
@Component(immediate = true)
public class DefaultTheme implements Theme {

	@Override
	public String[] getStyleSheets() {
		return new String[] { "/ui/css/default_theme/components.css", "/ui/css/default_theme/color_sets.css" };
	}

	@Override
	public String getLocalizedName() {
		return I18N.localize("org.to2mbn.lolixl.ui.impl.theme.default.name");
	}

	@Override
	public String[] getAuthors() {
		return new String[] { "LoliXL Developers" };
	}

	@Override
	public String getDescription() {
		return I18N.localize("org.to2mbn.lolixl.ui.impl.theme.default.description");
	}

	@Override
	public Image getIcon() {
		// TODO Use an icon here
		return null;
	}

}
