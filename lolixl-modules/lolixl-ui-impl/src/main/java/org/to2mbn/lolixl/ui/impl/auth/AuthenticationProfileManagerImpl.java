package org.to2mbn.lolixl.ui.impl.auth;

import static java.lang.String.format;
import static java.util.stream.Collectors.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
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
import org.to2mbn.lolixl.utils.GsonUtils;
import org.to2mbn.lolixl.utils.LambdaServiceTracker;
import org.to2mbn.lolixl.utils.ObservableContext;
import org.to2mbn.lolixl.utils.ServiceUtils;
import com.google.gson.JsonSyntaxException;

@Service({ AuthenticationProfileManager.class, ConfigurationCategory.class })
@Properties({
		@Property(name = ConfigurationCategory.PROPERTY_CATEGORY, value = "org.to2mbn.lolixl.ui.impl.auth.profiles")
})
@Component(immediate = true)
public class AuthenticationProfileManagerImpl implements AuthenticationProfileManager, ConfigurationCategory<AuthenticationProfileList> {

	private static final Logger LOGGER = Logger.getLogger(AuthenticationProfileManagerImpl.class.getCanonicalName());

	@Reference
	private EventAdmin eventAdmin;

	@Reference(target = "(usage=local_io)")
	private ExecutorService localIOPool;

	private BundleContext bundleContext;

	private Path profileBaseDir = Paths.get(".lolixl", "auth", "profiles");

	private AuthenticationProfileList profiles = new AuthenticationProfileList();

	private LambdaServiceTracker<AuthenticationService> serviceTracker;
	private ObservableContext observableContext;

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

	@Override
	public List<AuthenticationProfile<?>> getProfiles() {
		return profiles.entries.stream()
				.map(entry -> entry.profile)
				.filter(Objects::nonNull)
				.collect(toList());
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

		localIOPool.submit(() -> {
			saveProfile(entry);
		});
		observableContext.notifyChanged();

		return entry.profile;
	}

	@Override
	public void removeProfile(AuthenticationProfile<?> profile) {
		Objects.requireNonNull(profile);

		for (AuthenticationProfileEntry entry : profiles.entries) {
			if (entry.profile == profile) {

				if (profiles.entries.remove(entry)) {
					updateProfile(AuthenticationProfileEvent.TYPE_DELETE, entry);
					profile.setObservableContext(null);
					LOGGER.fine(() -> format("Removed auth profile %s", entry));

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
	}

	private void saveProfile(AuthenticationProfileEntry entry) {
		Path location = getProfileLocation(entry.uuid);
		try {
			synchronized (entry) {
				if (entry.profile == null) {
					throw new IllegalStateException("Profile is not initialized: " + entry);
				}
				GsonUtils.toJson(location, entry.profile.store());
				LOGGER.fine(() -> format("Saved auth profile %s", entry));
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, format("Couldn't save auth profile %s to [%s]", entry, location), e);
		}
	}

	private void loadAuthProfile(ServiceReference<AuthenticationService> reference, AuthenticationService service, AuthenticationProfileEntry entry) {
		AuthenticationProfile<?> profile;
		try {
			profile = service.createProfile();
			loadAuthProfileMemo(profile, getProfileLocation(entry.uuid));
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, format("Couldn't load auth profile [%s], skipping", entry.uuid), e);
			return;
		}

		synchronized (entry) {
			if (entry.profile == null) {
				doSetObservableContext(profile, entry);
				entry.serviceRef = reference;
				entry.profile = profile;
				updateProfile(AuthenticationProfileEvent.TYPE_CREATE, entry);
			}
		}
		LOGGER.fine(() -> format("Loaded auth profile %s, service=%s", entry, reference));
	}

	// 因为单独写会造成类型推断失败
	// 所以抽一个方法出来
	private <T extends java.io.Serializable> void loadAuthProfileMemo(AuthenticationProfile<T> profile, Path path) throws JsonSyntaxException, IOException {
		profile.restore(Optional.ofNullable(GsonUtils.fromJson(path, profile.getMementoType())));
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
	public void setObservableContext(ObservableContext ctx) {
		this.observableContext = ctx;
	}

	@Override
	public AuthenticationProfileList store() {
		return profiles;
	}

	@Override
	public void restore(Optional<AuthenticationProfileList> optionalMemento) {
		optionalMemento.ifPresent(memento -> profiles.entries.addAll(memento.entries));
		serviceTracker.open(true);
	}

	@Override
	public Class<? extends AuthenticationProfileList> getMementoType() {
		return AuthenticationProfileList.class;
	}
}
