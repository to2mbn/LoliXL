package org.to2mbn.lolixl.core.version.mcdir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.Version;
import org.to2mbn.jmccc.version.parsing.Versions;
import org.to2mbn.lolixl.core.game.version.GameVersion;
import javafx.scene.control.Button;

public class McdirGameVersion implements GameVersion {

	private String version;
	private Path mcdir;

	public McdirGameVersion(String name, Path mcdir) {
		this.version = name;
		this.mcdir = mcdir;
	}

	@Override
	public String getVersionNumber() {
		return version;
	}

	@Override
	public Path getMinecraftDirectory() {
		return mcdir;
	}

	@Override
	public Version getLaunchableVersion() {
		try {
			return Versions.resolveVersion(new MinecraftDirectory(mcdir.toFile()), version);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public Button createTile() {
		// TODO Auto-generated method stub
		return null;
	}

}
