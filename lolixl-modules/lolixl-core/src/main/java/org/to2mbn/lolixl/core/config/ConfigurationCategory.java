package org.to2mbn.lolixl.core.config;

import org.to2mbn.lolixl.core.ui.DisplayableItem;
import org.to2mbn.lolixl.utils.Storable;
import javafx.scene.layout.Region;

public interface ConfigurationCategory<CONF extends Configuration> extends Storable<CONF>, DisplayableItem {

	String PROPERTY_CATEGORY = "org.to2mbn.lolixl.core.config.category";

	void setConfigurationContext(ConfigurationContext ctx);

	Region createConfiguringPanel();

}
