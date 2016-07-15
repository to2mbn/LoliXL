package org.to2mbn.lolixl.plugin.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.lolixl.plugin.LocalPluginRepository;
import org.to2mbn.lolixl.plugin.maven.ArtifactNotFoundException;
import org.to2mbn.lolixl.plugin.maven.LocalMavenRepository;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;
import org.to2mbn.lolixl.plugin.maven.MavenRepository;
import org.to2mbn.lolixl.utils.AsyncUtils;

@Component
@Service({ LocalPluginRepository.class })
@Properties({
		@Property(name = "pluginRepo.type", value = "local")
})
public class LocalPluginRepositoryImpl extends AbstractPluginRepository implements LocalPluginRepository {

	@Reference(target = "(m2repository.type=local)")
	private LocalMavenRepository repository;

	private Map<String, Boolean> gav2isPlugin = new ConcurrentHashMap<>();
	private Path gav2isPluginFile;

	@Activate
	public void active() throws IOException {
		gav2isPluginFile = repository.getRootDir().resolve("gav2isPlugin.properties");
		java.util.Properties properties = new java.util.Properties();
		try (Reader reader = new InputStreamReader(Files.newInputStream(gav2isPluginFile), "UTF-8")) {
			properties.load(reader);
		}
		properties.forEach((k, v) -> gav2isPlugin.put(String.valueOf(k), Boolean.valueOf(String.valueOf(v))));
	}

	@Override
	public CompletableFuture<Void> downloadBundle(MavenRepository from, MavenArtifact artifact) {
		Objects.requireNonNull(from);
		Objects.requireNonNull(artifact);

		Boolean isPlugin = gav2isPlugin.get(artifact.toString());
		if (isPlugin != null) {
			if (Files.isRegularFile(repository.getArtifactPath(artifact, null, "jar"))
					&& (!isPlugin || Files.isRegularFile(repository.getArtifactPath(artifact, "lolixl-plugin", "xml")))) {
				return CompletableFuture.completedFuture(null);
			}
		}

		return repository.install(from, artifact, null, "jar")
				.thenCombine(
						repository.install(from, artifact, "lolixl-plugin", "xml")
								.handle((dummy, ex) -> {
									if (ex == null)
										return true;
									else if (AsyncUtils.exceptionInstanceof(ArtifactNotFoundException.class, ex))
										return false;
									else
										throw AsyncUtils.wrapWithCompletionException(ex);
								}),
						(dummy, isDescriptionDownloaded) -> {
							updateGav2isPlugin(artifact, isDescriptionDownloaded);
							return null;
						});
	}

	@Override
	public Stream<MavenArtifact> listPlugins() throws IOException {
		return repository.listArtifacts()
				.filter(artifact -> Files.isRegularFile(repository.getArtifactPath(artifact, "lolixl-plugin", "xml")));
	}

	@Override
	public LocalMavenRepository getRepository() {
		return repository;
	}

	private void updateGav2isPlugin(MavenArtifact artifact, boolean isPlugin) {
		gav2isPlugin.put(artifact.toString(), isPlugin);
		synchronized (gav2isPluginFile) {
			java.util.Properties properties = new java.util.Properties();
			properties.putAll(gav2isPlugin);
			try (Writer writer = new OutputStreamWriter(Files.newOutputStream(gav2isPluginFile), "UTF-8")) {
				properties.store(writer, "org.to2mbn.lolixl.plugin.impl.gav2isPlugin");
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

}
