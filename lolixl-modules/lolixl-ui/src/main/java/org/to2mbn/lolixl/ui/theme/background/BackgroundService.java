package org.to2mbn.lolixl.ui.theme.background;

import javafx.beans.value.ObservableObjectValue;
import javafx.collections.ObservableList;
import javafx.scene.layout.Background;

public interface BackgroundService {

	ObservableList<BackgroundProvider> getBackgroundProviders();

	ObservableObjectValue<BackgroundProvider> getCurrentBackgroundProvider();

	void selectBackgroundProvider(BackgroundProvider background);

	ObservableObjectValue<Background> getCurrentBackground();

}
