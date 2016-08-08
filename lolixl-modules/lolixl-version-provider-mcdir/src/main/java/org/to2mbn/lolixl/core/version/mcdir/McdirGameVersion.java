package org.to2mbn.lolixl.core.version.mcdir;

import com.sun.javafx.binding.ObjectConstant;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.image.Image;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.Version;
import org.to2mbn.jmccc.version.parsing.Versions;
import org.to2mbn.lolixl.core.game.version.GameVersion;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.component.view.version.GameVersionItemView;
import org.to2mbn.lolixl.utils.FXUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

public class McdirGameVersion implements GameVersion {

	private StringProperty aliasProperty = new SimpleStringProperty();
	private ObjectConstant<Image> icon = ObjectConstant.valueOf(new Image("/ui/img/grass_cube.png"));

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
	public StringProperty aliasProperty() {
		return aliasProperty;
	}

	@Override
	public Tile createTile() {
		Tile tile = new Tile();
		tile.setId("version-item-tile");
		GameVersionItemView view = new GameVersionItemView();
		view.versionNameLabel.textProperty().bind(getLocalizedName());
		view.iconView.imageProperty().bind(icon);
		// resolveTagsForVersion();
		FXUtils.setButtonGraphic(tile, view);
		return tile;
	}

	@Override
	public ObservableObjectValue<Image> getIcon() {
		return icon;
	}
}
