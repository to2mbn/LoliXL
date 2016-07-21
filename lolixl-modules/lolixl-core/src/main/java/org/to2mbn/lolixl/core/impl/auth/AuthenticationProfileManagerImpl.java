package org.to2mbn.lolixl.core.impl.auth;

import static java.lang.String.format;
import static java.util.stream.Collectors.*;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.to2mbn.lolixl.core.game.auth.AuthenticationProfile;
import org.to2mbn.lolixl.core.game.auth.AuthenticationProfileEvent;
import org.to2mbn.lolixl.core.game.auth.AuthenticationProfileManager;
import org.to2mbn.lolixl.core.game.auth.AuthenticationService;
import org.to2mbn.lolixl.utils.PathUtils;
import com.google.gson.Gson;

public class AuthenticationProfileManagerImpl implements AuthenticationProfileManager {

	private static final Logger LOGGER = Logger.getLogger(AuthenticationProfileManagerImpl.class.getCanonicalName());

	@Reference
	private Gson gson;

	@Reference
	private EventAdmin eventAdmin;

	@Reference(target = "(usage=local_io)")
	private ExecutorService localIOPool;

	private BundleContext bundleContext;

	private Path profileBaseDir = new File(".lolixl/auth/profiles").toPath();
	private Path profilesListFile = new File(".lolixl/auth/profiles-list.json").toPath();

	private AuthenticationProfileList profiles;

	private ServiceTracker<AuthenticationService, AuthenticationService> serviceTracker;// TODO

	@Activate
	public void active(ComponentContext compCtx) {
		bundleContext = compCtx.getBundleContext();
		tryReadProfilesListFile();
	}

	private void tryReadProfilesListFile() {
		if (Files.isRegularFile(profilesListFile)) {
			try (Reader reader = new InputStreamReader(Files.newInputStream(profilesListFile), "UTF-8")) {
				profiles = gson.fromJson(reader, AuthenticationProfileList.class);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, format("Couldn't read [%s]", profilesListFile), e);
			}
		}
		if (profiles == null) {
			profiles = new AuthenticationProfileList();
		}
		if (profiles.entries == null) {
			profiles.entries = new CopyOnWriteArrayList<>();
		}
	}

	@Override
	public List<AuthenticationProfile<?>> getProfiles() {
		return profiles.entries.stream()
				.map(entry -> entry.profile)
				.filter(Objects::nonNull)
				.collect(toList());
	}

	@Override
	public AuthenticationProfile<?> createProfile(ServiceReference<AuthenticationService> reference) {
		Objects.requireNonNull(reference);

		AuthenticationProfileEntry entry = new AuthenticationProfileEntry();

		entry.uuid = UUID.randomUUID();
		entry.serviceRef = reference;

		try {
			AuthenticationService service = bundleContext.getService(reference);
			entry.method = getAuthMethodName(reference, service);
			entry.profile = service.createProfile();
		} finally {
			bundleContext.ungetService(reference);
		}

		profiles.entries.add(entry);

		LOGGER.fine(format("Created authentication profile service=%s, uuid=[%s]", reference, entry.uuid));

		entry.profile.setObservableContext(() -> {
			updateProfile(AuthenticationProfileEvent.TYPE_UPDATE, entry);
			saveProfile(entry);
		});

		updateProfile(AuthenticationProfileEvent.TYPE_CREATE, entry);

		localIOPool.submit(() -> {
			saveProfilesList();
			saveProfile(entry);
		});

		return entry.profile;
	}

	private String getAuthMethodName(ServiceReference<AuthenticationService> reference, AuthenticationService service) {
		String method = (String) reference.getProperty(AuthenticationService.PROPERTY_AUTH_METHOD);
		if (method == null) {
			LOGGER.warning(format("No PROPERTY_AUTH_METHOD found for %s, using class name", reference));
			method = service.getClass().getName()
					.replace('$', '.');
		}
		return method;
	}

	@Override
	public void removeProfile(AuthenticationProfile<?> profile) {
		Objects.requireNonNull(profile);

		for (AuthenticationProfileEntry entry : profiles.entries) {
			if (entry.profile == profile) {
				if (profiles.entries.remove(entry)) {
					updateProfile(AuthenticationProfileEvent.TYPE_DELETE, entry);

					localIOPool.submit(() -> {
						saveProfilesList();
						try {
							Files.deleteIfExists(getProfileLocation(entry.uuid));
						} catch (Exception e) {
							LOGGER.log(Level.WARNING, format("Couldn't delete auth profile file for [%s]", entry.uuid), e);
						}
					});
					return;
				}
			}
		}
	}

	private void updateProfile(int type, AuthenticationProfileEntry entry) {
		eventAdmin.postEvent(new AuthenticationProfileEvent(type, entry.profile, entry.serviceRef));
	}

	private void saveProfile(AuthenticationProfileEntry entry) {
		Path location = getProfileLocation(entry.uuid);
		try {
			PathUtils.tryMkdirsParent(location);
			synchronized (entry) {
				if (entry.profile == null) {
					throw new IllegalStateException("Profile is not initialized");
				}
				Object memo = entry.profile.store();
				try (Writer writer = new OutputStreamWriter(Files.newOutputStream(location), "UTF-8")) {
					gson.toJson(memo, writer);
				}
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, format("Couldn't save auth profile [%s] to [%s]", entry.uuid, location), e);
		}
	}

	private void saveProfilesList() {
		try {
			PathUtils.tryMkdirsParent(profilesListFile);
			synchronized (profiles) {
				try (Writer writer = new OutputStreamWriter(Files.newOutputStream(profilesListFile), "UTF-8")) {
					gson.toJson(profiles, writer);
				}
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, format("Couldn't save profiles list to [%s]", profilesListFile), e);
		}
	}

	private Path getProfileLocation(UUID uuid) {
		return profileBaseDir.resolve(uuid.toString() + ".json");
	}

	@Override
	public Optional<ServiceReference<AuthenticationService>> getProfileProvider(AuthenticationProfile<?> profile) {
		for (AuthenticationProfileEntry entry : profiles.entries)
			if (entry.profile == profile)
				return Optional.ofNullable(entry.serviceRef);
		return Optional.empty();
	}

}
