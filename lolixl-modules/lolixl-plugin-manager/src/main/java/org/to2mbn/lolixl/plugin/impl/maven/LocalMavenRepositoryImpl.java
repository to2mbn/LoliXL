package org.to2mbn.lolixl.plugin.impl.maven;

import static java.lang.String.format;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.lolixl.plugin.impl.ReadToFileProcessor;
import org.to2mbn.lolixl.plugin.maven.ArtifactNotFoundException;
import org.to2mbn.lolixl.plugin.maven.ArtifactVersioning;
import org.to2mbn.lolixl.plugin.maven.LocalMavenRepository;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;
import org.to2mbn.lolixl.plugin.maven.MavenRepository;
import org.to2mbn.lolixl.plugin.util.MavenUtils;
import org.to2mbn.lolixl.utils.PathUtils;
import org.to2mbn.lolixl.utils.AsyncUtils;
import org.to2mbn.lolixl.utils.GsonUtils;

@Component
@Service({ LocalMavenRepository.class })
@Properties({
		@Property(name = "m2repository.type", value = "local")
})
public class LocalMavenRepositoryImpl implements LocalMavenRepository {

	private static final Logger LOGGER = Logger.getLogger(LocalMavenRepositoryImpl.class.getCanonicalName());

	@Reference(target = "(usage=local_io)")
	private ExecutorService localIOPool;

	private Path m2dir = Paths.get(".lolixl", "m2", "repo");

	@Override
	public CompletableFuture<Void> downloadArtifact(MavenArtifact artifact, String classifier, String type, Supplier<WritableByteChannel> output) {
		Objects.requireNonNull(artifact);
		Objects.requireNonNull(output);

		return asyncReadArtifact(getArtifactPath(artifact, classifier, type), output);
	}

	@Override
	public CompletableFuture<ArtifactVersioning> getVersioning(String groupId, String artifactId) {
		Objects.requireNonNull(groupId);
		Objects.requireNonNull(artifactId);

		return AsyncUtils.asyncRun(() -> {
			Path path = getVersioningMetadataPath(groupId, artifactId);
			checkArtifactExisting(path);
			return GsonUtils.fromJson(path, ArtifactVersioning.class);
		}, localIOPool);
	}

	@Override
	public Path getArtifactPath(MavenArtifact artifact, String classifier, String type) {
		Objects.requireNonNull(artifact);

		return getVersionDir(artifact).resolve(MavenUtils.getArtifactFileName(artifact, classifier, type));
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

	@Override
	public CompletableFuture<Void> install(MavenRepository from, MavenArtifact artifact, String classifier, String type) {
		Objects.requireNonNull(from);
		Objects.requireNonNull(artifact);

		LOGGER.fine(format("Installing %s classifier=%s type=%s from %s", artifact, classifier, type, from));
		return CompletableFuture.allOf(
				updateVersioning(from, artifact.getGroupId(), artifact.getArtifactId()),
				processDownloading(getArtifactPath(artifact, classifier, type), output -> from.downloadArtifact(artifact, classifier, type, output)));
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
			LOGGER.finer(format("Try reading [%s] to [%s]", path, output));
			checkArtifactExisting(path);

			try (FileChannel in = FileChannel.open(path, StandardOpenOption.READ);
					WritableByteChannel out = output.get();) {
				in.transferTo(0, in.size(), out);
			}

			return null;
		}, localIOPool);
	}

	private void checkArtifactExisting(Path path) throws ArtifactNotFoundException {
		if (!Files.exists(path)) {
			throw new ArtifactNotFoundException("Artifact file not found: " + path);
		}
	}

	private Path getVersioningMetadataPath(String groupId, String artifactId) {
		return getArtifactDir(groupId, artifactId).resolve("maven-metadata.json");
	}

	private CompletableFuture<Void> processDownloading(Path to, Function<Supplier<WritableByteChannel>, CompletableFuture<Void>> operation) {
		return new ReadToFileProcessor(to, operation).invoke();
	}

	private CompletableFuture<ArtifactVersioning> updateVersioning(MavenRepository from, String groupId, String artifactId) {
		return from.getVersioning(groupId, artifactId)
				.thenCompose(versioning -> AsyncUtils.asyncRun(() -> {
					GsonUtils.toJson(getVersioningMetadataPath(groupId, artifactId), versioning);
					return versioning;
				}, localIOPool));
	}

	@Override
	public Stream<MavenArtifact> listArtifacts() throws IOException {
		/*
		 * 列出所有artifact。
		 * 办法：
		 * 设一个artifact(groupId=G, artifactId=A, version=V)
		 * 在 ${G.replace('.','/')} / ${A} / ${V} 下就有 ${A}-${V}.jar
		 * 就是说
		 * 找出所有jar再筛选即可
		 */
		return Files.walk(m2dir)
				.filter(Files::isRegularFile)
				.map(p -> {
					List<String> elements = new ArrayList<>();
					m2dir.relativize(p).iterator().forEachRemaining(name -> elements.add(name.toString()));
					return elements;
				})
				.filter(l -> l.size() > 3)
				.map(l -> {
					String version = l.get(l.size() - 2);
					String artifactId = l.get(l.size() - 3);
					if (!l.get(l.size() - 1).equals(artifactId + "-" + version + ".jar"))
						return null;
					StringBuilder groupIdBuilder = new StringBuilder();
					for (int i = 0; i < l.size() - 3; i++)
						groupIdBuilder.append(l.get(i))
								.append('.');
					if (groupIdBuilder.length() > 0)
						groupIdBuilder.deleteCharAt(groupIdBuilder.length() - 1);
					return new MavenArtifact(groupIdBuilder.toString(), artifactId, version);
				})
				.filter(Objects::nonNull);
	}

	@Override
	public Path getRootDir() {
		return m2dir;
	}
}
