package org.to2mbn.lolixl.ui.impl.auth;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventAdmin;
import org.to2mbn.lolixl.core.config.ConfigurationCategory;
import org.to2mbn.lolixl.core.game.auth.AuthenticationProfile;
import org.to2mbn.lolixl.core.game.auth.AuthenticationProfileEvent;
import org.to2mbn.lolixl.core.game.auth.AuthenticationProfileManager;
import org.to2mbn.lolixl.core.game.auth.AuthenticationService;
import org.to2mbn.lolixl.ui.impl.auth.AuthenticationProfileList.AuthenticationProfileEntry;
import static org.to2mbn.lolixl.utils.FXUtils.checkFxThread;
import org.to2mbn.lolixl.utils.GsonUtils;
import org.to2mbn.lolixl.utils.LambdaServiceTracker;
import org.to2mbn.lolixl.utils.ObservableContext;
import org.to2mbn.lolixl.utils.ServiceUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Service({ AuthenticationProfileManager.class, ConfigurationCategory.class })
@Properties({
		@Property(name = ConfigurationCategory.PROPERTY_CATEGORY, value = AuthenticationProfileManagerImpl.CATEGORY_AUTH_PROFILES)
})
@Component(immediate = true)
public class AuthenticationProfileManagerImpl implements AuthenticationProfileManager, ConfigurationCategory<AuthenticationProfileList> {

	public static final String CATEGORY_AUTH_PROFILES = "org.to2mbn.lolixl.ui.impl.auth.profiles";

	private static final Logger LOGGER = Logger.getLogger(AuthenticationProfileManagerImpl.class.getCanonicalName());

	@Reference
	private EventAdmin eventAdmin;

	@Reference(target = "(usage=local_io)")
	private ExecutorService localIOPool;

	private BundleContext bundleContext;

	private Path profileBaseDir = Paths.get(".lolixl", "auth", "profiles");

	private AuthenticationProfileList profiles = new AuthenticationProfileList();

	private ObservableList<AuthenticationProfile<?>> profilesList = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
	private ObservableList<AuthenticationProfile<?>> profilesListView = FXCollections.unmodifiableObservableList(profilesList);

	private LambdaServiceTracker<AuthenticationService> serviceTracker;
	private ObservableContext observableContext;

	private ObjectProperty<AuthenticationProfile<?>> selectedProfileProperty = new SimpleObjectProperty<AuthenticationProfile<?>>() {

		@Override
		public void set(AuthenticationProfile<?> newValue) {
			checkFxThread();

			// Null is allowed to be set when, and only when, there is no available profiles and the uuid of previous selected profile is no long valid.
			if (newValue == null) {
				if (profiles.selectedProfile != null) {
					boolean isSelectedProfileValid = false;
					for (AuthenticationProfileEntry entry : profiles.entries) {
						if (Objects.equals(entry.uuid, profiles.selectedProfile)) {
							isSelectedProfileValid = true;
							break;
						}
					}
					if (!isSelectedProfileValid) {
						profiles.selectedProfile = null;
						super.set(null);
						observableContext.notifyChanged();
						return;
					}
				}

				throw new IllegalStateException("Null profile is not allowed to be set currently");
			}

			if (!profilesList.contains(newValue)) {
				throw new IllegalArgumentException("Auth profile " + newValue + " isn't managed");
			}
			super.set(newValue);
		}

		@Override
		protected void invalidated() {
			if (!Platform.isFxApplicationThread()) {
				Platform.runLater(this::invalidated);
				return;
			}
			AuthenticationProfile<?> selected = selectedProfileProperty.get();
			if (selected != null) {
				profiles.entries.stream().filter(entry -> entry.profile == selected).findFirst().map(entry -> entry.uuid).ifPresent(uuid -> {
					if (!Objects.equals(uuid, profiles.selectedProfile)) {
						LOGGER.fine("Updated selected auth profile: " + uuid);
						profiles.selectedProfile = uuid;
						observableContext.notifyChanged();
					}
				});
			}
		}

	};

	@Activate
	public void active(ComponentContext compCtx) {
		bundleContext = compCtx.getBundleContext();
		serviceTracker = new LambdaServiceTracker<>(bundleContext, AuthenticationService.class)
				.whenAdding((reference, service) -> profilesOfService(reference, service)
						.filter(entry -> entry.profile == null)
						.forEach(entry -> localIOPool.submit(
								() -> loadAuthProfile(reference, service, entry))))
				.whenRemoving((reference, service) -> profilesOfService(reference, service)
						.filter(entry -> entry.profile != null)
						.forEach(entry -> localIOPool.submit(
								() -> unloadAuthProfile(entry))));
	}

	@Deactivate
	public void deactive() {
		serviceTracker.close();
	}

	private void updateProfilesList() {
		profilesList.setAll(profiles.entries.stream()
				.map(entry -> entry.profile)
				.filter(Objects::nonNull)
				.collect(toList()));
	}

	@Override
	public Optional<ServiceReference<AuthenticationService>> getProfileProvider(AuthenticationProfile<?> profile) {
		for (AuthenticationProfileEntry entry : profiles.entries)
			if (entry.profile == profile)
				return Optional.ofNullable(entry.serviceRef);
		return Optional.empty();
	}

	@Override
	public AuthenticationProfile<?> createProfile(ServiceReference<AuthenticationService> reference) {
		Objects.requireNonNull(reference);
		checkFxThread();

		AuthenticationProfileEntry entry = new AuthenticationProfileEntry();

		entry.uuid = UUID.randomUUID();
		entry.serviceRef = reference;

		try {
			AuthenticationService service = bundleContext.getService(reference);
			entry.method = ServiceUtils.getIdProperty(AuthenticationService.PROPERTY_AUTH_METHOD, reference, service);
			entry.profile = service.createProfile();
		} finally {
			bundleContext.ungetService(reference);
		}

		profiles.entries.add(entry);

		LOGGER.fine(format("Created authentication profile %s, service=%s", entry, reference));

		doSetObservableContext(entry.profile, entry);

		updateProfile(AuthenticationProfileEvent.TYPE_CREATE, entry);

		saveProfile(entry);
		observableContext.notifyChanged();

		trySetDefaultSelectedProfile(entry);

		return entry.profile;
	}

	@Override
	public void removeProfile(AuthenticationProfile<?> profile) {
		Objects.requireNonNull(profile);
		checkFxThread();

		for (AuthenticationProfileEntry entry : profiles.entries) {
			if (entry.profile == profile) {

				if (profiles.entries.remove(entry)) {
					updateProfile(AuthenticationProfileEvent.TYPE_DELETE, entry);
					profile.setObservableContext(null);
					LOGGER.fine(() -> format("Removed auth profile %s", entry));

					if (Objects.equals(entry.uuid, profiles.selectedProfile)) {
						// find a successor
						if (getProfiles().size() > 0) {
							AuthenticationProfile<?> successor = getProfiles().get(0);
							selectedProfileProperty.set(successor);
						} else {
							selectedProfileProperty.set(null);
						}
					}
					checkSelectedProfileState();

					localIOPool.submit(() -> {
						try {
							Files.deleteIfExists(getProfileLocation(entry.uuid));
						} catch (Exception e) {
							LOGGER.log(Level.WARNING, format("Couldn't delete auth profile file for [%s]", entry.uuid), e);
						}
					});
					observableContext.notifyChanged();
					return;
				}
			}
		}
	}

	@Override
	public ObjectProperty<AuthenticationProfile<?>> selectedProfileProperty() {
		return selectedProfileProperty;
	}

	private void trySetDefaultSelectedProfile(AuthenticationProfileEntry entry) {
		if (profiles.selectedProfile == null) {
			selectedProfileProperty.set(entry.profile);
		}
		checkSelectedProfileState();
	}

	// self-check
	private void checkSelectedProfileState() {
		checkFxThread(); // should be invoked on javafx thread
		if (profiles.selectedProfile == null) {
			if (selectedProfileProperty.get() != null) {
				throw new IllegalStateException("selected profile uuid is null, but selected profile property is " + selectedProfileProperty.get());
			}
			for (AuthenticationProfileEntry entry : profiles.entries) {
				if (entry.profile != null) {
					throw new IllegalStateException("selected profile uuid is null, but profile " + entry + " is avaliable");
				}
			}
		} else {
			AuthenticationProfile<?> expectedProfile = null;
			boolean found = false;
			for (AuthenticationProfileEntry entry : profiles.entries) {
				if (Objects.equals(entry.uuid, profiles.selectedProfile)) {
					found = true;
					expectedProfile = entry.profile;
					break;
				}
			}
			if (!found) {
				throw new IllegalStateException("selected profile uuid is " + profiles.selectedProfile + " , but no such profile is found");
			}
			if (!Objects.equals(expectedProfile, selectedProfileProperty.get())) {
				throw new IllegalStateException("selected profile property doesn't match:\nexpected: " + expectedProfile + "\ncurrent: " + selectedProfileProperty.get());
			}
		}
	}

	private Stream<AuthenticationProfileEntry> profilesOfService(ServiceReference<AuthenticationService> reference, AuthenticationService service) {
		String authMethod = ServiceUtils.getIdProperty(AuthenticationService.PROPERTY_AUTH_METHOD, reference, service);
		return profiles.entries.stream()
				.filter(entry -> authMethod.equals(entry.method));
	}

	private void doSetObservableContext(AuthenticationProfile<?> profile, AuthenticationProfileEntry entry) {
		ObservableContext observableContext = new ObservableContext();
		observableContext.addListener(dummy -> {
			updateProfile(AuthenticationProfileEvent.TYPE_UPDATE, entry);
			saveProfile(entry);
		});
		profile.setObservableContext(observableContext);
	}

	private void updateProfile(int type, AuthenticationProfileEntry entry) {
		eventAdmin.postEvent(new AuthenticationProfileEvent(type, entry.profile, entry.serviceRef));
		updateProfilesList();
	}

	private void saveProfile(AuthenticationProfileEntry entry) {
		Path location = getProfileLocation(entry.uuid);
		AuthenticationProfile<?> profile = entry.profile;
		if (entry.profile == null) {
			throw new IllegalStateException("Profile is not initialized: " + entry);
		}

		localIOPool.submit(() -> {
			try {
				synchronized (entry) {
					GsonUtils.toJson(location, profile.store());
					LOGGER.fine(() -> format("Saved auth profile %s", entry));
				}
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, format("Couldn't save auth profile %s to [%s]", entry, location), e);
			}
		});
	}

	private void loadAuthProfile(ServiceReference<AuthenticationService> reference, AuthenticationService service, AuthenticationProfileEntry entry) {
		AuthenticationProfile<?> profile;
		try {
			profile = service.createProfile();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, format("Couldn't create auth profile [%s], skipping", entry.uuid), e);
			return;
		}

		loadAuthProfileMemo(profile, getProfileLocation(entry.uuid), entry.uuid)
				.thenRun(() -> Platform.runLater(() -> {
					synchronized (entry) {
						if (entry.profile == null) {
							doSetObservableContext(profile, entry);
							entry.serviceRef = reference;
							entry.profile = profile;
							updateProfile(AuthenticationProfileEvent.TYPE_CREATE, entry);
							if (Objects.equals(entry.uuid, profiles.selectedProfile)) {
								LOGGER.fine("Loading selected auth profile: " + entry.uuid);
								selectedProfileProperty.set(entry.profile);
							}
						}
					}
					LOGGER.fine(() -> format("Loaded auth profile %s, service=%s", entry, reference));
					trySetDefaultSelectedProfile(entry);
				}));
	}

	// 因为单独写会造成类型推断失败
	// 所以抽一个方法出来
	private <T extends java.io.Serializable> CompletableFuture<Void> loadAuthProfileMemo(AuthenticationProfile<T> profile, Path path, UUID uuid) {
		return GsonUtils.asynFromJson(path, profile.getMementoType())
				.handle((result, ex) -> {
					if (ex == null) {
						profile.restore(Optional.of(result));
					} else {
						LOGGER.log(Level.WARNING, "Couldn't load auth profile " + uuid, ex);
						profile.restore(Optional.empty());
					}
					return null;
				});
	}

	private void unloadAuthProfile(AuthenticationProfileEntry entry) {
		synchronized (entry) {
			if (entry.profile != null) {
				updateProfile(AuthenticationProfileEvent.TYPE_DELETE, entry);
				saveProfile(entry);
				entry.profile.setObservableContext(null);
				entry.profile = null;
				entry.serviceRef = null;
			}
		}
		LOGGER.fine(() -> format("Unloaded auth profile %s", entry));
	}

	private Path getProfileLocation(UUID uuid) {
		return profileBaseDir.resolve(uuid.toString() + ".json");
	}

	@Override
	public ObservableList<AuthenticationProfile<?>> getProfiles() {
		return profilesListView;
	}

	@Override
	public void setObservableContext(ObservableContext ctx) {
		this.observableContext = ctx;
	}

	@Override
	public AuthenticationProfileList store() {
		return profiles;
	}

	@Override
	public void restore(Optional<AuthenticationProfileList> optionalMemento) {
		optionalMemento.ifPresent(memento -> {
			profiles.entries.addAll(memento.entries);
			profiles.selectedProfile = memento.selectedProfile;
		});
		serviceTracker.open(true);
	}

	@Override
	public Class<? extends AuthenticationProfileList> getMementoType() {
		return AuthenticationProfileList.class;
	}
}
