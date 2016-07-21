package org.to2mbn.lolixl.core.version.mcdir;

import static java.util.stream.Collectors.*;
import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.parsing.Versions;
import org.to2mbn.lolixl.core.game.version.GameVersion;
import org.to2mbn.lolixl.core.game.version.GameVersionProvider;
import org.to2mbn.lolixl.i18n.I18N;

public class McdirGameVersionProvider implements GameVersionProvider {

	private Path location;

	public McdirGameVersionProvider(Path location) {
		this.location = location;
	}

	@Override
	public String getLocalizedName() {
		if (location.toAbsolutePath().equals(new File(".minecraft").toPath().toAbsolutePath())) {
			return I18N.localize("org.to2mbn.lolixl.core.version.mcdir.default.name");
		}
		return location.toString();
	}

	@Override
	public Map<String, GameVersion> listVersions() {
		return Versions.getVersions(new MinecraftDirectory(location.toFile())).stream()
				.collect(toMap(ver -> ver, ver -> new McdirGameVersion(ver, location)));
	}

	public String getLocationProperty() {
		return "mcdir:" + location;
	}

}
