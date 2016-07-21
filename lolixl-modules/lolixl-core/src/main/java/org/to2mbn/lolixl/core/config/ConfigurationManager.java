package org.to2mbn.lolixl.core.config;

import java.util.Optional;

public interface ConfigurationManager {

	Optional<Configuration> getConfiguration(String category);

}
