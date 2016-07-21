package org.to2mbn.lolixl.core.version.mcdir;

import static java.util.stream.Collectors.*;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.parsing.Versions;
import org.to2mbn.lolixl.core.game.version.GameVersion;
import org.to2mbn.lolixl.core.game.version.GameVersionProvider;
import org.to2mbn.lolixl.i18n.I18N;
import javafx.scene.control.Button;

public class McdirGameVersionProvider implements GameVersionProvider {

	private String alias;
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
	public List<GameVersion> getVersions() {
		// TODO: reuse GameVersions
		return Versions.getVersions(new MinecraftDirectory(location.toFile())).stream()
				.map(ver -> new McdirGameVersion(ver, location))
				.collect(toList());
	}

	public String getLocationProperty() {
		return "mcdir:" + location;
	}

	@Override
	public Button createTile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public void setAlias(String alias) {
		this.alias = alias;
	}

	@Override
	public Path getMinecraftDirectory() {
		return location;
	}

}
