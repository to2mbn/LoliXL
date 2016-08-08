package org.to2mbn.lolixl.core.impl.version;

import static org.to2mbn.lolixl.utils.FXUtils.checkFxThread;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.core.config.ConfigurationCategory;
import org.to2mbn.lolixl.core.game.version.GameVersion;
import org.to2mbn.lolixl.core.game.version.GameVersionProvider;
import org.to2mbn.lolixl.core.game.version.GameVersionProviderManager;
import org.to2mbn.lolixl.core.impl.version.GameVersionConfig.GameVersionProviderEntry;
import org.to2mbn.lolixl.utils.ObservableContext;
import org.to2mbn.lolixl.utils.ObservableServiceTracker;
import org.to2mbn.lolixl.utils.ServiceUtils;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;

@Service({ GameVersionProviderManager.class, ConfigurationCategory.class })
@Properties({
		@Property(name = ConfigurationCategory.PROPERTY_CATEGORY, value = GameVersionProviderManagerImpl.CATEGORY_VERSION_PROVIDERS)
})
@Component(immediate = true)
public class GameVersionProviderManagerImpl implements GameVersionProviderManager, ConfigurationCategory<GameVersionConfig> {

	public static final String CATEGORY_VERSION_PROVIDERS = "org.to2mbn.lolixl.ui.impl.version.manager";

	private static final Logger LOGGER = Logger.getLogger(GameVersionProviderManagerImpl.class.getCanonicalName());

	private ObservableServiceTracker<GameVersionProvider> providers;
	private Map<GameVersionProvider, String> locationMapping = new ConcurrentHashMap<>();
	private ObservableContext observableContext;

	private GameVersionConfig config = new GameVersionConfig();

	private ObjectProperty<GameVersion> selectedVersionProperty = new SimpleObjectProperty<GameVersion>() {

		@Override
		public void set(GameVersion newValue) {
			checkFxThread();

			if (newValue == null) {
				for (GameVersionProvider provider : getProviders()) {
					if (!provider.getVersions().isEmpty()) {
						throw new IllegalArgumentException("null is only allowed to be set when there is no successor");
					}
				}
				super.set(null);
				config.selected = null;
				observableContext.notifyChanged();
				return;
			}

			boolean checked = false;
			for (GameVersionProvider provider : getProviders()) {
				for (GameVersion version : provider.getVersions()) {
					if (version == newValue) {
						checked = true;
					}
				}
			}
			if (!checked) {
				throw new IllegalArgumentException("version " + newValue + " is not managed");
			}

			super.set(newValue);
		}

		@Override
		protected void invalidated() {
			if (!Platform.isFxApplicationThread()) {
				Platform.runLater(this::invalidated);
				return;
			}

			GameVersion newValue = selectedVersionProperty.get();
			if (newValue != null) {
				for (GameVersionProvider provider : getProviders()) {
					for (GameVersion version : provider.getVersions()) {
						if (version == newValue) {
							String location = locationMapping.get(provider);
							if (location == null) {
								throw new IllegalStateException("location for " + provider + " is not found in locationMapping");
							}
							SelectedGameVersion newSelected = new SelectedGameVersion(version.getVersionNumber(), location);
							if (!newSelected.equals(config.selected)) {
								config.selected = newSelected;
								LOGGER.info("Set selected version to " + newSelected);
								observableContext.notifyChanged();
							}
							return;
						}
					}
				}
			}
			throw new IllegalStateException("version " + newValue + " is not found");
		}

	};

	@Activate
	public void active(ComponentContext compCtx) {
		providers = new ObservableServiceTracker<>(compCtx.getBundleContext(), GameVersionProvider.class);
		providers
				.whenAdding((ref, service) -> {

					// process record
					String location = ServiceUtils.getIdProperty(GameVersionProvider.PROPERTY_PROVIDER_LOCATION, ref, service);
					locationMapping.put(service, location);
					GameVersionProviderEntry entry = config.providers.get(location);
					if (entry == null) {
						entry = new GameVersionProviderEntry();
						config.providers.put(location, entry);
						observableContext.notifyChanged();
					}
					LOGGER.fine("Added GameVersionProvider " + location);

					// process alias
					if (entry.alias != null) {
						service.aliasProperty().set(entry.alias);
						LOGGER.fine("Set alias for GameVersionProvider " + location + " : " + entry.alias);
					}
					GameVersionProviderEntry entry0 = entry; // for lambda
					service.aliasProperty().addListener((Observable dummy) -> {
						String newAlias = service.aliasProperty().get();
						if (!Objects.equals(newAlias, entry0.alias)) {
							entry0.alias = newAlias;
							LOGGER.fine("Alias of GameVersionProvider " + location + " changed to: " + newAlias);
							observableContext.notifyChanged();
						}
					});

					// process versions
					service.getVersions().addListener(new WeakListChangeListener<>(change -> {
						change.getAddedSubList().forEach(added -> {
							String versionNumber = added.getVersionNumber();
							String versionAlias = entry0.versionAlias.get(versionNumber);
							LOGGER.fine("Added version " + versionNumber + ", owner=" + location);
							if (versionAlias != null) {
								added.aliasProperty().set(versionAlias);
								LOGGER.fine("Set alias for version " + versionNumber + " : " + entry0.alias + ", owner=" + location);
							}
							added.aliasProperty().addListener((Observable dummy) -> {
								String newVersionAlias = added.aliasProperty().get();
								if (!Objects.equals(newVersionAlias, entry0.versionAlias.get(versionNumber))) {
									entry0.versionAlias.put(versionNumber, newVersionAlias);
									LOGGER.fine("Alias of version " + versionNumber + " changed to " + newVersionAlias + " , owner=" + location);
									observableContext.notifyChanged();
								}
							});

							boolean isSelectedVersion = config.selected != null &&
									Objects.equals(config.selected.getProviderName(), location) &&
									Objects.equals(config.selected.getVersionName(), versionNumber);
							boolean needSelectVersion = config.selected == null;
							if (isSelectedVersion || needSelectVersion) {
								selectedVersionProperty.set(added);
							}
						});
						change.getRemoved().forEach(removed -> entry0.versionAlias.remove(removed.getVersionNumber()));
					}));

					if (config.selected != null && Objects.equals(location, config.selected.getProviderName())) {
						// try to find out whether the selected version is available or not
						String selectedVersion = config.selected.getVersionName();
						boolean selectedAvailable = false;
						for (GameVersion version : service.getVersions()) {
							if (Objects.equals(version.getVersionNumber(), selectedVersion)) {
								selectedAvailable = true;

								// assert: the selected version should be resolved
								if (selectedVersionProperty.get() != version) {
									throw new IllegalStateException("The selected version " + selectedVersion + " is found in " + location + ", but selectedVersionProperty is " + selectedVersionProperty.get());
								}

								break;
							}
						}
						if (!selectedAvailable) {
							// need to find a successor
							boolean found = false;
							findSuccessor:
							for (GameVersionProvider provider : getProviders()) {
								for (GameVersion version : provider.getVersions()) {
									selectedVersionProperty.set(version);
									found = true;
									break findSuccessor;
								}
							}
							if (!found) {
								selectedVersionProperty.set(null);
							}
						}
					}
				})
				.whenRemoving((ref, service) -> locationMapping.remove(service));
	}

	@Override
	public ObjectProperty<GameVersion> selectedVersionProperty() {
		return selectedVersionProperty;
	}

	@Override
	public ObservableList<GameVersionProvider> getProviders() {
		return providers.getServiceList();
	}

	@Override
	public GameVersionConfig store() {
		return config;
	}

	@Override
	public void restore(Optional<GameVersionConfig> memento) {
		memento.ifPresent(memo -> {
			config.providers.putAll(memo.providers);
			config.selected = memo.selected;
		});
	}

	@Override
	public Class<? extends GameVersionConfig> getMementoType() {
		return GameVersionConfig.class;
	}

	@Override
	public void setObservableContext(ObservableContext ctx) {
		observableContext = ctx;
	}

}
