package org.to2mbn.lolixl.core.game.configuration.simple;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

public class SimpleGameConfiguration implements GameConfiguration {

	private static final Logger LOGGER = Logger.getLogger(SimpleGameConfiguration.class.getCanonicalName());
	private static final long serialVersionUID = 1L;

	public RuntimeDirectoryStrategy runtimeDirStrategy = RuntimeDirectoryStrategy.DEFAULT;
	public String customizedRuntimeDir;

	public MemoryStrategy xmxStrategy = MemoryStrategy.AUTOMATIC;
	public int customizedXmx;

	public MemoryStrategy xmsStrategy = MemoryStrategy.UNDEFINED;
	public int customizedXms;

	public JavaPathStrategy javaPathStrategy = JavaPathStrategy.AUTOMATIC;
	public String customizedJavaPath;

	public Set<DefaultArgumentOption> defaultArgumentOptions;

	public boolean disableOSXArguments = false;

	public String server;

	public WindowSizeStrategy windowSizeStrategy = WindowSizeStrategy.DEFAULT;
	public int customizedWindowWidth;
	public int customizedWindowHeight;

	public List<String> customizedJvmArguments;
	public List<String> customizedMinecraftArguments;
	public Map<String, String> customizedCommandlineVariables;
	public Set<String> customizedClasspath;

	@Override
	public LaunchOption process(AuthenticationProfile<?> authentication, GameVersion versionToLaunch) {
		LaunchOption option = new LaunchOption(versionToLaunch.getLaunchableVersion(), authentication.getAuthenticator(), new MinecraftDirectory(versionToLaunch.getMinecraftDirectory().toFile()));

		switch (runtimeDirStrategy) {
			case CUSTOMIZED:
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

		switch (xmxStrategy) {
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
				option.setMaxMemory(customizedXmx);
				break;

			case UNDEFINED:
				option.setMaxMemory(0);
				break;
		}

		switch (xmsStrategy) {
			case AUTOMATIC:
			case UNDEFINED:
				option.setMinMemory(0);
				break;

			case CUSTOMIZED:
				option.setMinMemory(customizedXms);
				break;
		}

		switch (javaPathStrategy) {
			case CUSTOMIZED:
				if (customizedJavaPath == null)
					throw new IllegalArgumentException("customizedJavaPath is not set");
				option.setJavaEnvironment(new JavaEnvironment(new File(customizedJavaPath)));
				break;

			default:
				break;
		}

		Set<String> automaticJvmArgs = new LinkedHashSet<>();

		if (defaultArgumentOptions != null) {
			for (DefaultArgumentOption defaultArgumentOption : defaultArgumentOptions) {
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
		}

		if (!disableOSXArguments && Platform.CURRENT == Platform.OSX) {
			automaticJvmArgs.add(ExtraArgumentsTemplates.OSX_DOCK_NAME);
			try {
				automaticJvmArgs.add(ExtraArgumentsTemplates.OSX_DOCK_ICON(option.getMinecraftDirectory(), option.getVersion()));
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, "Couldn't append JVM argument OSX_DOCK_ICON", e);
			}
		}

		if (customizedJvmArguments != null) {
			automaticJvmArgs.removeAll(customizedJvmArguments);
		}

		option.extraJvmArguments().addAll(automaticJvmArgs);

		if (customizedJvmArguments != null) {
			option.extraJvmArguments().addAll(customizedJvmArguments);
		}

		if (customizedMinecraftArguments != null) {
			option.extraMinecraftArguments().addAll(customizedMinecraftArguments);
		}

		if (customizedCommandlineVariables != null) {
			option.commandlineVariables().putAll(customizedCommandlineVariables);
		}

		if (customizedClasspath != null) {
			customizedClasspath.stream()
					.map(File::new)
					.forEach(option.extraClasspath()::add);
		}

		if (server != null) {
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

		switch (windowSizeStrategy) {
			case FULLSCREEN:
				option.setWindowSize(new WindowSize(customizedWindowWidth, customizedWindowHeight));
				break;

			case CUSTOMIZED:
				option.setWindowSize(WindowSize.fullscreen());
				break;

			default:
				break;
		}

		return option;
	}

}
