package org.to2mbn.lolixl.impl.init;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Constants;
import org.to2mbn.lolixl.plugin.maven.ArtifactNotFoundException;
import org.to2mbn.lolixl.plugin.maven.ArtifactVersioning;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;
import org.to2mbn.lolixl.plugin.maven.MavenRepository;
import org.to2mbn.lolixl.plugin.util.MavenUtils;
import org.to2mbn.lolixl.utils.AsyncUtils;

@Component
@Service({ MavenRepository.class })
@Properties({
		@Property(name = "m2repository.chain", value = "remote"),
		@Property(name = Constants.SERVICE_RANKING, intValue = Integer.MIN_VALUE)
})
public class InternalMavenRepository implements MavenRepository {

	@Reference(target = "(usage=local_io)")
	private ExecutorService localIOPool;

	@Reference(target = "(usage=cpu_compute)")
	private ExecutorService cpuComputePool;

	private Method methodOpenChannel;
	private Method methodGetVersion;

	public InternalMavenRepository() throws NoSuchMethodException, ClassNotFoundException {
		Class<?> endpoint = Class.forName("org.to2mbn.lolixl.main.AccessEndpoint", true, ClassLoader.getSystemClassLoader());
		methodOpenChannel = endpoint.getDeclaredMethod("openChannel", String.class, String.class, String.class, String.class, String.class);
		methodGetVersion = endpoint.getDeclaredMethod("getVersion", String.class, String.class);
	}

	@Override
	public CompletableFuture<Void> downloadArtifact(MavenArtifact artifact, String classifier, String type, Supplier<WritableByteChannel> output) {
		return AsyncUtils.asyncRun(() -> {
			try (FileChannel in = openChannel(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), classifier, type)) {
				if (in == null) {
					throw new ArtifactNotFoundException();
				}
				try (WritableByteChannel out = output.get()) {
					in.transferTo(0, in.size(), out);
				}
			}
			return null;
		}, localIOPool);
	}

	@Override
	public CompletableFuture<ArtifactVersioning> getVersioning(String groupId, String artifactId) {
		return AsyncUtils.asyncRun(() -> {
			String version = getVersion(groupId, artifactId);
			if (version == null) {
				throw new ArtifactNotFoundException(groupId + ":" + artifactId);
			}

			SortedSet<String> versions = new TreeSet<>();
			versions.add(version);
			versions = Collections.unmodifiableSortedSet(versions);
			if (MavenUtils.isSnapshot(version)) {
				return new ArtifactVersioning(version, version, versions);
			} else {
				return new ArtifactVersioning(version, null, versions);
			}
		}, cpuComputePool);
	}

	private FileChannel openChannel(String groupId, String artifactId, String version, String classifier, String type) throws InvocationTargetException, IllegalAccessException, IllegalArgumentException {
		return (FileChannel) methodOpenChannel.invoke(null, groupId, artifactId, version, classifier, type);
	}

	private String getVersion(String groupId, String artifactId) throws InvocationTargetException, IllegalAccessException, IllegalArgumentException {
		return (String) methodGetVersion.invoke(null, groupId, artifactId);
	}
}
