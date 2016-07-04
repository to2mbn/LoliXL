package org.to2mbn.lolixl.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class Metadata {

	public static final String M2_GROUP_ID = "org.to2mbn.lolixl";
	public static final String M2_ARTIFACT_ID = "lolixl-package";
	public static String M2_VERSION;

	public static final String LOLIXL_NAME = "LoliXL";

	private static final String RESOURCE_M2_PROPERTIES = "/META-INF/maven/" + M2_GROUP_ID + "/" + M2_ARTIFACT_ID + "/pom.properties";

	static void initMetadata() throws IOException {
		try {
			M2_VERSION = readM2Version();
		} catch (Throwable e) {
			M2_VERSION = "unknown";
			throw e;
		}
	}

	private static String readM2Version() throws IOException {
		try (InputStream in = Metadata.class.getResourceAsStream(RESOURCE_M2_PROPERTIES)) {
			if (in == null) {
				throw new IOException("Maven配置文件不存在");
			}
			Properties properties = new Properties();
			properties.load(in);
			String version = properties.getProperty("version");
			if (version == null) {
				throw new IOException("Maven配置中不存在version");
			}
			return version;
		}
	}

}
