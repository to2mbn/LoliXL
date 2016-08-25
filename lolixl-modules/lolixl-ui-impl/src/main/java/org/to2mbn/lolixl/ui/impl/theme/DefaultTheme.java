package org.to2mbn.lolixl.ui.impl.theme;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.ui.theme.Theme;
import org.to2mbn.lolixl.utils.binding.FxConstants;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;

@Service({ Theme.class })
@Properties({
		@Property(name = Theme.PROPERTY_THEME_ID, value = "org.to2mbn.lolixl.ui.theme.default"),
		@Property(name = Theme.PROPERTY_THEME_TYPE, value = Theme.TYPE_THEME_PACKAGE)
})
@Component(immediate = true)
public class DefaultTheme implements Theme {

	@Override
	public String[] getStyleSheets() {
		return new String[] { "css/org.to2mbn.lolixl.ui.theme.default/color_set.css" };
	}

	@Override
	public ObservableStringValue getLocalizedName() {
		return I18N.localize("org.to2mbn.lolixl.ui.theme.default.name");
	}

	@Override
	public ObservableValue<ObservableStringValue[]> getAuthors() {
		return FxConstants.object(new ObservableStringValue[] { I18N.localize("org.to2mbn.lolixl.ui.theme.default.author.lolixlgroup") });
	}

	@Override
	public ObservableStringValue getDescription() {
		return I18N.localize("org.to2mbn.lolixl.ui.theme.default.description");
	}

	@Override
	public ObservableObjectValue<Image> getIcon() {
		return FxConstants.object(null);
	}

}
