package org.to2mbn.lolixl.ui.theme.background;

import org.to2mbn.lolixl.ui.DisplayableTile;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.layout.Background;

public interface BackgroundProvider extends DisplayableTile {

	String PROPERTY_BACKGROUND_ID = "org.to2mbn.lolixl.ui.theme.background.id";

	ObservableObjectValue<Background> getBackground();

}
