package org.to2mbn.lolixl.core.config;

import org.to2mbn.lolixl.core.ui.DisplayableItem;
import org.to2mbn.lolixl.utils.Storable;
import javafx.scene.layout.Region;

public interface ConfigurationCategory<CONF extends Configuration> extends Storable<CONF>, DisplayableItem {

	String PROPERTY_AUTH_METHOD = "org.to2mbn.lolixl.core.config.category";

	void setConfigurationContext(ConfigurationContext ctx);

	Region createConfiguringPanel();

}
