package org.to2mbn.lolixl.plugin.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
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

	@Override
	public CompletableFuture<Void> downloadBundle(MavenRepository from, MavenArtifact artifact) {
		Objects.requireNonNull(from);
		Objects.requireNonNull(artifact);

		return CompletableFuture.allOf(
				repository.install(from, artifact, null, "jar"),
				repository.install(from, artifact, "lolixl-plugin", "xml")
						.exceptionally(ex -> {
							if (AsyncUtils.exceptionInstanceof(ArtifactNotFoundException.class, ex))
								return null;
							throw AsyncUtils.wrapWithCompletionException(ex);
						}));
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

}
