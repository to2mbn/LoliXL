package org.to2mbn.lolixl.main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.wiring.BundleRevision;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

class Main {

	private static final Logger LOGGER = Logger.getLogger(Main.class.getCanonicalName());
	private static final String RESOURCE_FELIX_CONFIGURATION = "/org.to2mbn.lolixl.felix.properties";

	private static Felix felix = null;
	private static Properties felixConfiguration = null;

	private static Properties loadConfiguration() throws IOException {
		Properties configuration = new Properties();
		try (InputStream in = Main.class.getResourceAsStream(RESOURCE_FELIX_CONFIGURATION)) {
			if (in == null) {
				throw new IOException("无法加载Felix配置: " + RESOURCE_FELIX_CONFIGURATION);
			}
			configuration.load(new InputStreamReader(in, "UTF-8"));
		}
		return configuration;
	}

	/*
	 * lolixl.readPluginToMem 在 Intel Core i5-4200H CPU @ 2.80GHz × 4 上的测试结果
	 * 
	 * 参数                            t0            t1              T
	 * lolixl.readPluginToMem=true     23:52:55.456  23:52:57.604    2.148s
	 * lolixl.readPluginToMem=false    23:54:18.218  23:54:20.133    1.915s
	 * 
	 * Notes:
	 * 若lolixl.readPluginToMem为true，则先将插件jar的数据读入堆内存，再作为bundle加载
	 * 若lolixl.readPluginToMem为false，则直接将插件从磁盘中作为bundle加载
	 */

	private static void setupSystemProperties() {
		System.setProperty("org.to2mbn.lolixl.version", Metadata.M2_VERSION);
		System.setProperty("lolixl.readPluginToMem", "true");
		System.setProperty("lolixl.forciblyExit", "true");
		System.setProperty("lolixl.overwriteSystemPlugins", "true");
		System.setProperty("lolixl.hackCss", "true");
		System.setProperty("org.ehcache.sizeof.AgentSizeOf.bypass", "true");
	}

	private static void setupWorkingDir() throws IOException {
		File lolixlDir = new File(".lolixl");
		if (lolixlDir.exists()) {
			if (lolixlDir.isFile()) {
				JOptionPane.showMessageDialog(null, "请将当前目录下的.lolixl文件删除后再启动。", Metadata.LOLIXL_NAME, JOptionPane.ERROR_MESSAGE);
				throw new IOException("当前目录下存在.lolixl文件，而不是目录");
			}
		} else {
			if (!lolixlDir.mkdirs()) {
				throw new IOException("无法建立.lolixl目录");
			}
		}
	}

	private static void setupFelix(Felix felix) throws Exception {
		AccessEndpoint.internalBundleRepository.init(felix);
	}

	private static void configureJUL() throws IOException {
		Handler loggingHandler = new FileHandler(Metadata.LOG_FILE);
		loggingHandler.setFormatter(new SimpleFormatter());
		loggingHandler.setLevel(Level.ALL);

		Logger rootLogger = Logger.getLogger("");
		rootLogger.addHandler(loggingHandler);
		rootLogger.setLevel(Level.FINE);
		for (Handler handler : rootLogger.getHandlers()) {
			if (handler instanceof ConsoleHandler) {
				handler.setLevel(Level.WARNING);
			}
		}
	}

	private static void processConfiguration(Properties configuration) {
		configuration.put(FelixConstants.LOG_LOGGER_PROP, new FelixLoggerAdapter());
		LOGGER.fine("Felix configuration: " + configuration);
	}

	public static void main(String[] args) {
		try {
			Metadata.initMetadata();
			setupSystemProperties();
			setupWorkingDir();
			configureJUL();

			LOGGER.fine("System properties: " + System.getProperties());

			initFx();

			felixConfiguration = loadConfiguration();
			processConfiguration(felixConfiguration);
			AccessEndpoint.internalBundleRepository = new InternalBundleRepository();

			clearFelixCache();
			felix = new Felix(felixConfiguration);
			LOGGER.fine("Felix capabilities: " + felix.adapt(BundleRevision.class).getCapabilities(null));
			felix.start();
			OSGiListener osgiListener = new OSGiListener();
			felix.getBundleContext().addBundleListener(osgiListener);
			felix.getBundleContext().addFrameworkListener(osgiListener);
			setupFelix(felix);

			FrameworkEvent event;
			do {
				event = felix.waitForStop(Long.MAX_VALUE);
				if ((event.getType() & FrameworkEvent.ERROR) == FrameworkEvent.ERROR) {
					throw new IllegalStateException("Felix stopped because of an error: " + event, event.getThrowable());
				}
			} while (event.getType() == FrameworkEvent.WAIT_TIMEDOUT ||
					event.getType() == FrameworkEvent.STOPPED_UPDATE);
			shutdown();
		} catch (Throwable e) {
			if (felix != null) {
				try {
					felix.stop();
				} catch (Throwable e1) {
					e.addSuppressed(e1);
				}
			}
			shutdown();
			FatalErrorReporter.process(e);
			System.exit(1);
		}
	}

	private static void shutdown() {
		try {
			Platform.exit();
		} catch (Throwable e) {
			// Some java runtimes don't have JavaFX, e.g. OpenJDK
			LOGGER.log(Level.SEVERE, "Couldn't stop JavaFX", e);
		}
		clearFelixCache();
	}

	private static void clearFelixCache() {
		try {
			if (felixConfiguration != null) {
				String cachePath = felixConfiguration.getProperty("felix.cache.rootdir");
				if (cachePath != null) {
					LOGGER.fine("Cleaning " + cachePath);
					deleteRecursively(Paths.get(cachePath));
				}
			}
		} catch (Throwable e) {
			LOGGER.log(Level.WARNING, "Couldn't clean up felix cache", e);
		}
	}

	private static void deleteRecursively(Path path) throws IOException {
		if (Files.isDirectory(path)) {
			try {
				Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Files.deleteIfExists(file);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						Files.deleteIfExists(dir);
						return FileVisitResult.CONTINUE;
					}

				});
			} catch (NoSuchFileException e) {}
		} else if (Files.isRegularFile(path)) {
			Files.deleteIfExists(path);
		}
	}

	private static void initFx() {
		new Thread(() -> {
			try {
				LOGGER.info("Initializing JavaFX");
				new JFXPanel(); // init JavaFX
			} catch (Throwable e) {
				LOGGER.log(Level.SEVERE, "Couldn't init JavaFX", e);
			}
		}, "JavaFx-init").start();
	}
}
