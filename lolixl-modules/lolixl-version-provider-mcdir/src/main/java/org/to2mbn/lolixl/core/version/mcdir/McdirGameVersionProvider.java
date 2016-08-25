package org.to2mbn.lolixl.core.version.mcdir;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.version.parsing.Versions;
import org.to2mbn.lolixl.core.game.version.GameVersion;
import org.to2mbn.lolixl.core.game.version.GameVersionProvider;
import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.utils.binding.FxConstants;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class McdirGameVersionProvider implements GameVersionProvider {

	private StringProperty aliasProperty = new SimpleStringProperty();
	private Path location;
	private Runnable remover;
	private ObservableList<GameVersion> gameVersions = FXCollections.observableArrayList();
	private ObservableList<GameVersion> gameVersionsReadOnlyView = FXCollections.unmodifiableObservableList(gameVersions);
	private Map<String, GameVersion> versionMapping = new ConcurrentHashMap<>();

	public McdirGameVersionProvider(Path location, Runnable remover) {
		this.location = location;
		this.remover = remover;
		refreshVersions();
	}

	@Override
	public ObservableStringValue getLocalizedName() {
		if (isCurrentMcdir()) {
			return I18N.localize("org.to2mbn.lolixl.mcdir.default.name");
		}
		return FxConstants.string(location.toString());
	}

	@Override
	public ObservableList<GameVersion> getVersions() {
		return gameVersionsReadOnlyView;
	}

	@Override
	public Path getMinecraftDirectory() {
		return location;
	}

	@Override
	public StringProperty aliasProperty() {
		return aliasProperty;
	}

	public void refreshVersions() {
		Set<String> newVersions = Versions.getVersions(new MinecraftDirectory(location.toFile()));
		synchronized (versionMapping) {
			Set<String> diffAdd = new LinkedHashSet<>(newVersions);
			diffAdd.removeAll(versionMapping.keySet());

			Set<String> diffRemove = new LinkedHashSet<>(versionMapping.keySet());
			diffRemove.removeAll(newVersions);

			for (String toRemove : diffRemove) {
				gameVersions.remove(versionMapping.remove(toRemove));
			}

			for (String toAdd : diffAdd) {
				GameVersion ver = new McdirGameVersion(toAdd, location);
				versionMapping.put(toAdd, ver);
				gameVersions.add(ver);
			}
		}
	}

	public boolean isCurrentMcdir() {
		return location.toAbsolutePath().equals(Paths.get(".minecraft").toAbsolutePath());
	}

	public int getRanking() {
		if (isCurrentMcdir()) {
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public void delete() {
		remover.run();
	}

}
