package org.to2mbn.lolixl.plugin.impl;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.lolixl.plugin.LocalPluginRepository;
import org.to2mbn.lolixl.plugin.maven.LocalMavenRepository;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;
import org.to2mbn.lolixl.plugin.maven.MavenRepository;

@Component
@Service({ LocalPluginRepository.class })
@Properties({
		@Property(name = "pluginRepo.type", value = "local")
})
public class LocalPluginRepositoryImpl extends AbstractPluginRepository implements LocalPluginRepository {

	private static final String[][] ARTIFACTS_TO_DOWNLOAD = new String[][] {
			new String[] { null, "jar" },
			new String[] { null, "jar.asc" },
			new String[] { "lolixl-plugin", "xml" },
			new String[] { "lolixl-plugin", "xml.asc" }
	};

	@Reference(target = "(m2repository.type=local)")
	private LocalMavenRepository repository;

	@Override
	public CompletableFuture<Void> downloadBundle(MavenRepository from, MavenArtifact artifact) {
		Objects.requireNonNull(from);
		Objects.requireNonNull(artifact);

		@SuppressWarnings("unchecked")
		CompletableFuture<Void>[] futures = new CompletableFuture[ARTIFACTS_TO_DOWNLOAD.length];
		for (int i = 0; i < ARTIFACTS_TO_DOWNLOAD.length; i++) {
			String classifier = ARTIFACTS_TO_DOWNLOAD[i][0];
			String type = ARTIFACTS_TO_DOWNLOAD[i][1];
			futures[i] = Objects.requireNonNull(repository.install(from, artifact, classifier, type));
		}
		return CompletableFuture.allOf(futures);
	}

	@Override
	public LocalMavenRepository getRepository() {
		return repository;
	}

}
