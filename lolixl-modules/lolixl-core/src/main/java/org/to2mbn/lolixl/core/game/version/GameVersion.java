package org.to2mbn.lolixl.core.game.version;

import java.nio.file.Path;
import org.to2mbn.jmccc.version.Version;
import org.to2mbn.lolixl.ui.model.DisplayableTile;
import org.to2mbn.lolixl.utils.Aliasable;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;

public interface GameVersion extends DisplayableTile, Aliasable {

	ObservableStringValue getVersionNumber();

	Path getMinecraftDirectory();
	Version getLaunchableVersion();

	@Override
	default ObservableStringValue getLocalizedName() {
		return new StringBinding() {

			StringProperty aliasProperty = aliasProperty();
			ObservableStringValue versionNumber = getVersionNumber();

			{
				bind(aliasProperty, versionNumber);
			}

			@Override
			protected String computeValue() {
				String alias = aliasProperty.get();
				if (alias == null) {
					return versionNumber.get();
				}
				return alias;
			}
		};
	}

}
