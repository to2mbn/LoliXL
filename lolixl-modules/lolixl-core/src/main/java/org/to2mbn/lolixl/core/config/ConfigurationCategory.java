package org.to2mbn.lolixl.core.config;

import org.to2mbn.lolixl.ui.model.DisplayableItem;
import org.to2mbn.lolixl.utils.Observable;
import org.to2mbn.lolixl.utils.Storable;
import javafx.scene.layout.Region;

public interface ConfigurationCategory<CONF extends Configuration> extends Storable<CONF>, DisplayableItem, Observable {

	String PROPERTY_CATEGORY = "org.to2mbn.lolixl.core.config.category";

	Region createConfiguringPanel();

}
