package org.to2mbn.lolixl.plugin.impl.maven;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.lolixl.plugin.MavenArtifact;
import org.to2mbn.lolixl.plugin.maven.ArtifactNotFoundException;
import org.to2mbn.lolixl.plugin.maven.ArtifactSnapshot;
import org.to2mbn.lolixl.plugin.maven.ArtifactVersioning;
import org.to2mbn.lolixl.plugin.maven.LocalMavenRepository;
import org.to2mbn.lolixl.plugin.util.MavenUtils;
import org.to2mbn.lolixl.plugin.util.PathUtils;
import org.to2mbn.lolixl.utils.AsyncUtils;
import com.google.gson.Gson;

@Component
@Service({ LocalMavenRepository.class })
@Properties({
		@Property(name = "m2repository.type", value = "local")
})
public class LocalMavenRepositoryImpl implements LocalMavenRepository {

	@Reference(target = "(usage=local_io)")
	private ExecutorService localIOPool;

	@Reference
	private Gson gson;

	private Path m2dir = new File(".lolixl/m2/repo").toPath();

	@Override
	public CompletableFuture<Void> downloadRelease(MavenArtifact artifact, String classifier, String type, Supplier<WritableByteChannel> output) {
		Objects.requireNonNull(artifact);
		Objects.requireNonNull(output);
		MavenUtils.requireRelease(artifact);

		return asyncReadArtifact(getReleasePath(artifact, classifier, type), output);
	}

	@Override
	public CompletableFuture<Void> downloadSnapshot(MavenArtifact artifact, ArtifactSnapshot snapshot, String classifier, String type, Supplier<WritableByteChannel> output) {
		Objects.requireNonNull(artifact);
		Objects.requireNonNull(snapshot);
		Objects.requireNonNull(output);
		MavenUtils.requireSnapshot(artifact);

		return asyncReadArtifact(getSnapshotPath(artifact, snapshot, classifier, type), output);
	}

	@Override
	public CompletableFuture<ArtifactVersioning> getVersioning(String groupId, String artifactId) {
		Objects.requireNonNull(groupId);
		Objects.requireNonNull(artifactId);

		return readMetadataJson(ArtifactVersioning.class, getArtifactDir(groupId, artifactId).resolve("maven-metadata.json"));
	}

	@Override
	public CompletableFuture<ArtifactSnapshot> getSnapshot(MavenArtifact artifact) {
		Objects.requireNonNull(artifact);
		MavenUtils.requireSnapshot(artifact);

		return readMetadataJson(ArtifactSnapshot.class, getVersionDir(artifact).resolve("maven-metadata.json"));
	}

	@Override
	public Path getReleasePath(MavenArtifact artifact, String classifier, String type) {
		Objects.requireNonNull(artifact);
		MavenUtils.requireRelease(artifact);

		return getVersionDir(artifact).resolve(MavenUtils.getArtifactFileName(artifact, classifier, type));
	}

	@Override
	public Path getSnapshotPath(MavenArtifact artifact, ArtifactSnapshot snapshot, String classifier, String type) {
		Objects.requireNonNull(artifact);
		Objects.requireNonNull(snapshot);
		MavenUtils.requireSnapshot(artifact);

		return getVersionDir(artifact).resolve(MavenUtils.getArtifactFileName(artifact, snapshot, classifier, type));
	}

	@Override
	public CompletableFuture<Void> deleteArtifact(MavenArtifact artifact) {
		Objects.requireNonNull(artifact);

		return AsyncUtils.asyncRun(() -> {
			PathUtils.deleteRecursively(getVersionDir(artifact));
			return null;
		}, localIOPool);
	}

	@Override
	public CompletableFuture<Void> deleteArtifactAllVersions(String groupId, String artifactId) {
		Objects.requireNonNull(groupId);
		Objects.requireNonNull(artifactId);

		return AsyncUtils.asyncRun(() -> {
			PathUtils.deleteRecursively(getArtifactDir(groupId, artifactId));
			return null;
		}, localIOPool);
	}

	private Path getArtifactDir(String groupId, String artifactId) {
		Path p = m2dir;
		for (String gid : groupId.split("\\."))
			p = p.resolve(gid);
		return p.resolve(artifactId);
	}

	private Path getVersionDir(MavenArtifact artifact) {
		return getArtifactDir(artifact.getGroupId(), artifact.getArtifactId())
				.resolve(artifact.getVersion());
	}

	private CompletableFuture<Void> asyncReadArtifact(Path path, Supplier<WritableByteChannel> output) {
		return AsyncUtils.asyncRun(() -> {
			checkArtifactExisting(path);

			try (FileChannel in = FileChannel.open(path, StandardOpenOption.READ);
					WritableByteChannel out = output.get();) {
				in.transferTo(0, in.size(), out);
			}

			return null;
		}, localIOPool);
	}

	private <T> CompletableFuture<T> readMetadataJson(Class<T> clazz, Path path) {
		return AsyncUtils.asyncRun(() -> {
			checkArtifactExisting(path);

			try (Reader reader = new InputStreamReader(Files.newInputStream(path), "UTF-8")) {
				return gson.fromJson(reader, clazz);
			}
		}, localIOPool);
	}

	private void checkArtifactExisting(Path path) throws ArtifactNotFoundException {
		if (!Files.exists(path)) {
			throw new ArtifactNotFoundException("Artifact file not found: " + path);
		}
	}

}
