package org.to2mbn.lolixl.core.game.configuration.simple;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.to2mbn.jmccc.option.JavaEnvironment;
import org.to2mbn.jmccc.option.LaunchOption;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.option.ServerInfo;
import org.to2mbn.jmccc.option.WindowSize;
import org.to2mbn.jmccc.util.ExtraArgumentsTemplates;
import org.to2mbn.jmccc.util.Platform;
import org.to2mbn.lolixl.core.game.auth.AuthenticationProfile;
import org.to2mbn.lolixl.core.game.configuration.GameConfiguration;
import org.to2mbn.lolixl.core.game.version.GameVersion;
import org.to2mbn.lolixl.utils.MemoryTools;
import org.to2mbn.lolixl.utils.ObservableContext;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;

public class SimpleGameConfiguration implements GameConfiguration {

	private static final Logger LOGGER = Logger.getLogger(SimpleGameConfiguration.class.getCanonicalName());
	private static final long serialVersionUID = 1L;

	private StringProperty aliasProperty = new SimpleStringProperty();

	private ObjectProperty<RuntimeDirectoryStrategy> runtimeDirStrategyProperty = new SimpleObjectProperty<>(RuntimeDirectoryStrategy.DEFAULT);
	private StringProperty customizedRuntimeDirProperty = new SimpleStringProperty();

	private ObjectProperty<MemoryStrategy> xmxStrategyProperty = new SimpleObjectProperty<>(MemoryStrategy.AUTOMATIC);
	private IntegerProperty customizedXmxProperty = new SimpleIntegerProperty();

	private ObjectProperty<MemoryStrategy> xmsStrategyProperty = new SimpleObjectProperty<>(MemoryStrategy.UNDEFINED);
	private IntegerProperty customizedXmsProperty = new SimpleIntegerProperty();

	private ObjectProperty<JavaPathStrategy> javaPathStrategyProperty = new SimpleObjectProperty<>(JavaPathStrategy.AUTOMATIC);
	private StringProperty customizedJavaPathProperty = new SimpleStringProperty();

	private SetProperty<DefaultArgumentOption> defaultArgumentOptionsProperty = new SimpleSetProperty<>(FXCollections.observableSet(new TreeSet<>()));

	private BooleanProperty disableOSXArgumentsProperty = new SimpleBooleanProperty(false);

	private StringProperty serverProperty = new SimpleStringProperty();

	private ObjectProperty<WindowSizeStrategy> windowSizeStrategyProperty = new SimpleObjectProperty<>(WindowSizeStrategy.DEFAULT);
	private IntegerProperty customizedWindowWidthProperty = new SimpleIntegerProperty();
	private IntegerProperty customizedWindowHeightProperty = new SimpleIntegerProperty();

	private ListProperty<String> customizedJvmArgumentsProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
	private ListProperty<String> customizedMinecraftArgumentsProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
	private MapProperty<String, String> customizedCommandlineVariablesProperty = new SimpleMapProperty<>(FXCollections.observableMap(new LinkedHashMap<>()));
	private SetProperty<String> customizedClasspathProperty = new SimpleSetProperty<>(FXCollections.observableSet(new TreeSet<>()));

	@Override
	public LaunchOption process(AuthenticationProfile<?> authentication, GameVersion versionToLaunch) {
		LaunchOption option = new LaunchOption(versionToLaunch.getLaunchableVersion(), authentication.getAuthenticator(), new MinecraftDirectory(versionToLaunch.getMinecraftDirectory().toFile()));

		switch (runtimeDirStrategyProperty.get()) {
			case CUSTOMIZED:
				String customizedRuntimeDir = customizedRuntimeDirProperty.get();
				if (customizedRuntimeDir == null)
					throw new IllegalArgumentException("customizedRuntimeDir is not set");
				option.setRuntimeDirectory(new MinecraftDirectory(customizedRuntimeDir));
				break;

			case ISOLATED:
				option.setRuntimeDirectory(new MinecraftDirectory(option.getMinecraftDirectory().getVersion(option.getVersion().getVersion())));
				break;

			default:
				break;
		}

		switch (xmxStrategyProperty.get()) {
			case AUTOMATIC:
				int xmxToSet;
				try {
					xmxToSet = MemoryTools.computeSuggestedXmx();
				} catch (Throwable e) {
					LOGGER.log(Level.WARNING, "Couldn't compute suggested xmx, using UNDEFINED strategy", e);
					xmxToSet = 0;
				}
				option.setMaxMemory(xmxToSet);
				break;

			case CUSTOMIZED:
				option.setMaxMemory(customizedXmxProperty.get());
				break;

			case UNDEFINED:
				option.setMaxMemory(0);
				break;
		}

		switch (xmsStrategyProperty.get()) {
			case AUTOMATIC:
			case UNDEFINED:
				option.setMinMemory(0);
				break;

			case CUSTOMIZED:
				option.setMinMemory(customizedXmsProperty.get());
				break;
		}

		switch (javaPathStrategyProperty.get()) {
			case CUSTOMIZED:
				String customizedJavaPath = customizedJavaPathProperty.get();
				if (customizedJavaPath == null)
					throw new IllegalArgumentException("customizedJavaPath is not set");
				option.setJavaEnvironment(new JavaEnvironment(new File(customizedJavaPath)));
				break;

			default:
				break;
		}

		Set<String> automaticJvmArgs = new LinkedHashSet<>();

		for (DefaultArgumentOption defaultArgumentOption : defaultArgumentOptionsProperty) {
			switch (defaultArgumentOption) {
				case FML_IGNORE_INVALID_MINECRAFT_CERTIFICATES:
					automaticJvmArgs.add(ExtraArgumentsTemplates.FML_IGNORE_INVALID_MINECRAFT_CERTIFICATES);
					break;

				case FML_IGNORE_PATCH_DISCREPANCISE:
					automaticJvmArgs.add(ExtraArgumentsTemplates.FML_IGNORE_PATCH_DISCREPANCISE);
					break;

				default:
					LOGGER.warning("Unknown DefaultArgumentOption: " + defaultArgumentOption);
					break;
			}
		}

		if (!disableOSXArgumentsProperty.get() && Platform.CURRENT == Platform.OSX) {
			automaticJvmArgs.add(ExtraArgumentsTemplates.OSX_DOCK_NAME);
			try {
				automaticJvmArgs.add(ExtraArgumentsTemplates.OSX_DOCK_ICON(option.getMinecraftDirectory(), option.getVersion()));
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, "Couldn't append JVM argument OSX_DOCK_ICON", e);
			}
		}

		automaticJvmArgs.removeAll(customizedJvmArgumentsProperty);
		option.extraJvmArguments().addAll(automaticJvmArgs);
		option.extraJvmArguments().addAll(customizedJvmArgumentsProperty);

		option.extraMinecraftArguments().addAll(customizedMinecraftArgumentsProperty);

		option.commandlineVariables().putAll(customizedCommandlineVariablesProperty);

		customizedClasspathProperty.stream()
				.map(File::new)
				.forEach(option.extraClasspath()::add);

		String server = serverProperty.getValueSafe().trim();
		if (!server.isEmpty()) {
			int idxColon = server.lastIndexOf(':');
			String host = server.substring(0, idxColon);
			int port = 0;
			if (idxColon != -1) {
				try {
					port = Integer.valueOf(host.substring(idxColon + 1));
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Illegal port", e);
				}
			}
			option.setServerInfo(new ServerInfo(host, port));
		}

		switch (windowSizeStrategyProperty.get()) {
			case FULLSCREEN:
				option.setWindowSize(new WindowSize(customizedWindowWidthProperty.get(), customizedWindowHeightProperty.get()));
				break;

			case CUSTOMIZED:
				option.setWindowSize(WindowSize.fullscreen());
				break;

			default:
				break;
		}

		return option;
	}

	@Override
	public void setObservableContext(ObservableContext ctx) {
		ctx.bind(aliasProperty,
				runtimeDirStrategyProperty,
				customizedRuntimeDirProperty,
				xmxStrategyProperty,
				customizedXmxProperty,
				xmsStrategyProperty,
				customizedXmsProperty,
				javaPathStrategyProperty,
				customizedJavaPathProperty,
				defaultArgumentOptionsProperty,
				disableOSXArgumentsProperty,
				serverProperty,
				windowSizeStrategyProperty,
				customizedWindowWidthProperty,
				customizedWindowHeightProperty,
				customizedJvmArgumentsProperty,
				customizedMinecraftArgumentsProperty,
				customizedCommandlineVariablesProperty,
				customizedClasspathProperty);
	}

	@Override
	public StringProperty aliasProperty() {
		return aliasProperty;
	}

	public ObjectProperty<RuntimeDirectoryStrategy> runtimeDirStrategyProperty() {
		return runtimeDirStrategyProperty;
	}

	public StringProperty customizedRuntimeDirProperty() {
		return customizedRuntimeDirProperty;
	}

	public ObjectProperty<MemoryStrategy> xmxStrategyProperty() {
		return xmxStrategyProperty;
	}

	public IntegerProperty customizedXmxProperty() {
		return customizedXmxProperty;
	}

	public ObjectProperty<MemoryStrategy> xmsStrategyProperty() {
		return xmsStrategyProperty;
	}

	public IntegerProperty customizedXmsProperty() {
		return customizedXmsProperty;
	}

	public ObjectProperty<JavaPathStrategy> javaPathStrategyProperty() {
		return javaPathStrategyProperty;
	}

	public StringProperty customizedJavaPathProperty() {
		return customizedJavaPathProperty;
	}

	public SetProperty<DefaultArgumentOption> defaultArgumentOptionsProperty() {
		return defaultArgumentOptionsProperty;
	}

	public BooleanProperty disableOSXArgumentsProperty() {
		return disableOSXArgumentsProperty;
	}

	public StringProperty serverProperty() {
		return serverProperty;
	}

	public ObjectProperty<WindowSizeStrategy> windowSizeStrategyProperty() {
		return windowSizeStrategyProperty;
	}

	public IntegerProperty customizedWindowWidthProperty() {
		return customizedWindowWidthProperty;
	}

	public IntegerProperty customizedWindowHeightProperty() {
		return customizedWindowHeightProperty;
	}

	public ListProperty<String> customizedJvmArgumentsProperty() {
		return customizedJvmArgumentsProperty;
	}

	public ListProperty<String> customizedMinecraftArgumentsProperty() {
		return customizedMinecraftArgumentsProperty;
	}

	public MapProperty<String, String> customizedCommandlineVariablesProperty() {
		return customizedCommandlineVariablesProperty;
	}

	public SetProperty<String> customizedClasspathProperty() {
		return customizedClasspathProperty;
	}
}
