package org.to2mbn.lolixl.core.game.version;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;

public interface GameVersionProviderManager {

	ObservableList<GameVersionProvider> getProviders();

	ObjectProperty<GameVersion> selectedVersionProperty();

}
