package org.to2mbn.lolixl.ui.impl.theme.background;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.ui.image.ImageLoading;
import org.to2mbn.lolixl.ui.theme.background.BackgroundProvider;
import org.to2mbn.lolixl.ui.theme.background.ImageBackgroundProvider;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.image.Image;

@Service({ BackgroundProvider.class })
@Properties({
		@Property(name = BackgroundProvider.PROPERTY_BACKGROUND_ID, value = "org.to2mbn.lolixl.ui.theme.background.default")
})
@Component(immediate = true)
public class DefaultBackground extends ImageBackgroundProvider {

	@Override
	public ObservableStringValue getLocalizedName() {
		return I18N.localize("org.to2mbn.lolixl.ui.theme.background.default.name");
	}

	@Override
	public ObservableObjectValue<Image> getBackgroundImage() {
		return ImageLoading.load("img/org.to2mbn.lolixl.ui.theme.background.default/background.jpg");
	}

}
