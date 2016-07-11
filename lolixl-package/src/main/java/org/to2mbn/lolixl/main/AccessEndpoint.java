package org.to2mbn.lolixl.main;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Map;
import org.osgi.framework.Bundle;

public final class AccessEndpoint {

	private AccessEndpoint() {}

	static InternalBundleRepository internalBundleRepository;

	public static FileChannel openChannel(String groupId, String artifactId, String version, String classifier, String type) throws IOException {
		return internalBundleRepository.openChannel(groupId, artifactId, version, classifier, type);
	}

	public static String getVersion(String groupId, String artifactId) {
		return internalBundleRepository.getVersion(groupId, artifactId);
	}

	public static Map<String, Bundle> getGav2bootstrapBundles() {
		return internalBundleRepository.getGav2bootstrapBundles();
	}

}
