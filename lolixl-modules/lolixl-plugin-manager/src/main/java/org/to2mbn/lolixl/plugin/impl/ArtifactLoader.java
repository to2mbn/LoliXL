package org.to2mbn.lolixl.plugin.impl;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.to2mbn.lolixl.plugin.LocalPluginRepository;
import org.to2mbn.lolixl.plugin.PluginDescription;
import org.to2mbn.lolixl.plugin.gpg.GPGVerifier;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;

public class ArtifactLoader {

	private byte[] jar;
	private URI jarUri;
	private Optional<PluginDescription> description;

	private LocalPluginRepository repository;
	private MavenArtifact artifact;

	// TODO: We are not going to verify gpg signature currently.
	//       But we may do this in the future.
	//       So keep the field here.
	@SuppressWarnings("unused")
	private GPGVerifier verifier;

	public ArtifactLoader(GPGVerifier verifier, LocalPluginRepository repository, MavenArtifact artifact) {
		this.verifier = Objects.requireNonNull(verifier);
		this.repository = Objects.requireNonNull(repository);
		this.artifact = Objects.requireNonNull(artifact);
	}

	public ArtifactLoader(byte[] jar, Optional<PluginDescription> description) {
		this.jar = jar;
		this.description = description;
	}

	public Optional<byte[]> getJar() {
		return Optional.ofNullable(jar);
	}

	public URI getJarURI() {
		return jarUri;
	}

	public Optional<PluginDescription> getDescription() {
		return description;
	}

	public CompletableFuture<ArtifactLoader> load(boolean loadDirectlyFromFile) {
		if (repository == null)
			throw new IllegalStateException("Couldn't to load because of missing repository");

		jarUri = repository.getRepository().getArtifactPath(artifact, null, "jar").toUri();
		return CompletableFuture.allOf(
				repository.getPluginDescription(artifact)
						.thenAccept(description -> this.description = description),
				loadDirectlyFromFile
						? CompletableFuture.completedFuture(null)
						: readArtifact(null, "jar")
								.thenAccept(data -> this.jar = data))
				.thenApply(dummy -> this);
	}

	private CompletableFuture<byte[]> readArtifact(String classifier, String type) {
		return new ReadToMemoryProcessor(output -> repository.getRepository().downloadArtifact(artifact, classifier, type, output))
				.invoke();
	}

}
