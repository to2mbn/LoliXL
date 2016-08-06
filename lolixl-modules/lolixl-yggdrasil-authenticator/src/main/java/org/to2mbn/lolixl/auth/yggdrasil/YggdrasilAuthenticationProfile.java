package org.to2mbn.lolixl.auth.yggdrasil;

import static org.to2mbn.lolixl.utils.FXUtils.*;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.Authenticator;
import org.to2mbn.jmccc.auth.yggdrasil.CharacterSelector;
import org.to2mbn.jmccc.auth.yggdrasil.YggdrasilAuthenticator;
import org.to2mbn.jmccc.auth.yggdrasil.core.AuthenticationService;
import org.to2mbn.jmccc.auth.yggdrasil.core.GameProfile;
import org.to2mbn.jmccc.auth.yggdrasil.core.ProfileService;
import org.to2mbn.jmccc.auth.yggdrasil.core.Session;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.Texture;
import org.to2mbn.jmccc.auth.yggdrasil.core.texture.TextureType;
import org.to2mbn.jmccc.mojangapi.MojangAPI;
import org.to2mbn.lolixl.core.game.auth.AuthenticationProfile;
import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.utils.ObservableContext;
import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Region;

public class YggdrasilAuthenticationProfile implements AuthenticationProfile<YggdrasilProfileMemo> {

	private static final Logger LOGGER = Logger.getLogger(YggdrasilAuthenticationProfile.class.getCanonicalName());

	private ObservableContext observableContext;

	private AuthenticationService authService;
	private Optional<ProfileService> profileService;
	private Optional<MojangAPI> mojangApi;
	private Executor executor;
	private Function<String, String> passwordProvider;
	private CharacterSelector characterSelector;

	private StringProperty emailProperty = new SimpleStringProperty();
	private ObjectProperty<GameProfile> selectedProfileProperty = new SimpleObjectProperty<>();
	private ObjectProperty<Map<TextureType, Texture>> texturesProperty = new SimpleObjectProperty<>();

	private ObservableStringValue i18nNoName = I18N.localize("org.to2mbn.lolixl.auth.yggdrasil.profile.name.noname");

	private ObservableStringValue usernameView = new StringBinding() {

		{
			bind(emailProperty, selectedProfileProperty, i18nNoName);
		}

		@Override
		protected String computeValue() {
			GameProfile profile = selectedProfileProperty.get();
			if (profile != null) {
				return profile.getName();
			}

			String email = emailProperty.get();
			if (isEmailValid(email)) {
				return email;
			}

			return i18nNoName.get();
		}
	};

	private YggdrasilAuthenticator authenticator;

	public YggdrasilAuthenticationProfile(AuthenticationService authService, ProfileService profileService, MojangAPI mojangApi, Executor executor, Function<String, String> passwordProvider, CharacterSelector characterSelector) {
		this.profileService = Optional.ofNullable(profileService);
		this.mojangApi = Optional.ofNullable(mojangApi);
		this.executor = Objects.requireNonNull(executor);
		this.passwordProvider = Objects.requireNonNull(passwordProvider);
		this.characterSelector = Objects.requireNonNull(characterSelector);

		this.authenticator = new YggdrasilAuthenticator(authService) {

			private static final long serialVersionUID = 1L;

			@Override
			protected PasswordProvider tryPasswordLogin() throws AuthenticationException {
				return new PasswordProvider() {

					@Override
					public String getUsername() throws AuthenticationException {
						return requireValidEmail();
					}

					@Override
					public String getPassword() throws AuthenticationException {
						return passwordProvider.apply(requireValidEmail());
					}

					@Override
					public CharacterSelector getCharacterSelector() {
						return characterSelector;
					}

					private String requireValidEmail() throws AuthenticationException {
						String email = emailProperty.get();
						if (!isEmailValid(email)) {
							throw new AuthenticationException("Invalid email");
						}
						return email;
					}
				};
			}

		};
	}

	@Override
	public Region createConfiguringPanel() {
		// TODO: createConfiguringPanel for YggdrasilAuthenticationProfile
		return null;
	}

	// Business logic
	public void update(String email, Optional<String> password) {
		Objects.requireNonNull(email);
		Objects.requireNonNull(password);
		checkFxThread();

		if (!isEmailValid(email)) {
			throw new IllegalArgumentException("email cannot be empty");
		}

		boolean isEmailChanged = !Objects.equals(emailProperty.get(), email);

		// Update email
		emailProperty.set(email);

		// Profile **may** be changed, mark current profile unknown
		// It's just a surmise
		if (isEmailChanged) {
			selectedProfileProperty.set(null);
			texturesProperty.set(null);
		}

		// Login
		executor.execute(() -> {
			try {
				if (password.isPresent()) {
					authenticator.refreshWithPassword(email, password.get(), characterSelector);
				} else {
					authenticator.refresh();
				}
			} catch (AuthenticationException e) {
				// failure
				LOGGER.log(Level.WARNING, "Login failure", e);
				Platform.runLater(() -> handleException(e));
				return;
			}

			// success
			Platform.runLater(() -> {
				Session session = authenticator.getCurrentSession();
				if (session != null) {
					selectedProfileProperty.set(session.getSelectedProfile());
				}
				updateTextures();
			});
		});
	}

	private void updateTextures() {
		profileService.ifPresent(existingProfileService -> {
			GameProfile profile = selectedProfileProperty.get();
			if (profile != null) {
				executor.execute(() -> {
					Map<TextureType, Texture> textures;
					try {
						textures = existingProfileService.getTextures(profile);
					} catch (AuthenticationException e) {
						// failure

						// Don't make the exception a big event
						// Keep silent and make money

						// Sorry, I'm a western reporter
						LOGGER.log(Level.WARNING, "Couldn't fetch textures of " + profile, e);

						// TODO: Maybe we should try again?
						//       like invoking updateTextures() again?
						return;
					}

					// success
					Platform.runLater(() -> {
						// compare and set
						if (profile.equals(selectedProfileProperty.get())) {
							texturesProperty.set(textures);
						}
					});
				});
			}
		});
	}

	private void handleException(AuthenticationException exception) {
		// TODO: handle authentication exception
	}

	private boolean isEmailValid(String email) {
		return email != null && !email.trim().isEmpty();
	}

	// ==== Getters & Setters ====
	@Override
	public ObservableStringValue getUsername() {
		return usernameView;
	}

	@Override
	public ObservableStringValue getEmail() {
		return emailProperty;
	}

	@Override
	public ObservableValue<Map<TextureType, Texture>> getTextures() {
		return texturesProperty;
	}

	@Override
	public void setObservableContext(ObservableContext ctx) {
		this.observableContext = ctx;
	}

	@Override
	public Authenticator getAuthenticator() {
		return authenticator;
	}

	// ==== Persistence ===
	@Override
	public YggdrasilProfileMemo store() {
		return new YggdrasilProfileMemo(emailProperty.get(), authenticator.getCurrentSession());
	}

	@Override
	public void restore(Optional<YggdrasilProfileMemo> memento) {
		memento.ifPresent(memo -> {
			emailProperty.set(memo.email);
			authenticator.setCurrentSession(memo.session);
		});
	}

	@Override
	public Class<? extends YggdrasilProfileMemo> getMementoType() {
		return YggdrasilProfileMemo.class;
	}

}
