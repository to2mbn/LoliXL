package org.to2mbn.lolixl.main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import javax.swing.JOptionPane;
import org.apache.felix.framework.Felix;

class Main {

	private static final String RESOURCE_FELIX_CONFIGRATION = "/org.to2mbn.lolixl.felix.properties";

	private static Properties loadConfigration() throws IOException {
		Properties configration = new Properties();
		try (InputStream in = Main.class.getResourceAsStream(RESOURCE_FELIX_CONFIGRATION)) {
			if (in == null) {
				throw new IOException("无法加载Felix配置: " + RESOURCE_FELIX_CONFIGRATION);
			}
			configration.load(new InputStreamReader(in, "UTF-8"));
		}
		return configration;
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
		InternalBundleRepository repo = new InternalBundleRepository();
		repo.init(felix);
	}

	public static void main(String[] args) {
		Felix felix = null;
		try {
			Metadata.initMetadata();
			setupSystemProperties();
			Properties felixConfigration = loadConfigration();
			setupWorkingDir();

			felix = new Felix(felixConfigration);
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