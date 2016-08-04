package org.to2mbn.lolixl.ui;

import org.to2mbn.lolixl.ui.model.ConfigurationCategoryViewProvider;
import javafx.collections.ObservableList;

public interface ConfigurationCategoryViewManager {

	ObservableList<ConfigurationCategoryViewProvider> getProviders();

}
