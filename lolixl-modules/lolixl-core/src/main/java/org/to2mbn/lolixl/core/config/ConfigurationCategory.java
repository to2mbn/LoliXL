package org.to2mbn.lolixl.core.config;

import org.to2mbn.lolixl.utils.ObservableContextAware;
import org.to2mbn.lolixl.utils.Storable;

public interface ConfigurationCategory<CONF extends Configuration> extends Storable<CONF>, ObservableContextAware {

	String PROPERTY_CATEGORY = "org.to2mbn.lolixl.core.config.category";

}
