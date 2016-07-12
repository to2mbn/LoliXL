package org.to2mbn.lolixl.main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;

class Main {

	private static final String RESOURCE_FELIX_CONFIGURATION = "/org.to2mbn.lolixl.felix.properties";

	private static FileHandler loggingHandler;

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

	private static void setupSystemProperties() {
		System.setProperty("org.to2mbn.lolixl.version", Metadata.M2_VERSION);
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

	private static void setupLoggingHandler() throws IOException {
		loggingHandler = new FileHandler(Metadata.LOG_FILE);
		loggingHandler.setFormatter(new SimpleFormatter("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL [%4$s] [%3$s] %5$s%6$s%n"));
		loggingHandler.setLevel(Level.ALL);
	}

	private static void configureJUL() throws IOException {
		setupLoggingHandler();
		enableLogging("org.to2mbn", Level.ALL);
		enableLogging("org.apache.felix", Level.ALL);
	}

	private static void enableLogging(String packageName, Level level) {
		Logger logger = Logger.getLogger(packageName);
		Handler handler = new Handler() {

			@Override
			public void publish(LogRecord record) {
				if (isLoggable(record))
					loggingHandler.publish(record);
			}

			@Override
			public void flush() {
				loggingHandler.flush();
			}

			@Override
			public void close() throws SecurityException {
				loggingHandler.close();
			}
		};
		handler.setLevel(level);
		logger.addHandler(handler);
		logger.setLevel(level);
	}

	private static void processConfiguration(Properties configuration) {
		configuration.put(FelixConstants.LOG_LOGGER_PROP, new FelixLoggerAdapter());
	}

	public static void main(String[] args) {
		Felix felix = null;
		try {
			Metadata.initMetadata();
			setupSystemProperties();
			setupWorkingDir();
			configureJUL();
			Properties felixConfiguration = loadConfiguration();
			processConfiguration(felixConfiguration);
			AccessEndpoint.internalBundleRepository = new InternalBundleRepository();

			felix = new Felix(felixConfiguration);
			felix.start();
			setupFelix(felix);
			felix.waitForStop(Long.MAX_VALUE);
		} catch (Throwable e) {
			if (felix != null) {
				try {
					felix.stop();
				} catch (Throwable e1) {
					e.addSuppressed(e1);
				}
			}
			FatalErrorReporter.process(e);
			System.exit(1);
		}
	}

}
