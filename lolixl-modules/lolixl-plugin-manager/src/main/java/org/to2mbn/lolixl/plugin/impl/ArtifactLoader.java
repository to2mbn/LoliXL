package org.to2mbn.lolixl.plugin.impl;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.to2mbn.lolixl.plugin.PluginDescription;
import org.to2mbn.lolixl.plugin.PluginRepository;
import org.to2mbn.lolixl.plugin.gpg.GPGVerifier;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;

public class ArtifactLoader {

	private byte[] jar;
	private Optional<PluginDescription> description;

	private PluginRepository repository;
	private MavenArtifact artifact;

	// TODO: We are not going to verify gpg signature currently.
	//       But we may do this in the future.
	//       So keep the field here.
	@SuppressWarnings("unused")
	private GPGVerifier verifier;

	public ArtifactLoader(GPGVerifier verifier, PluginRepository repository, MavenArtifact artifact) {
		this.verifier = Objects.requireNonNull(verifier);
		this.repository = Objects.requireNonNull(repository);
		this.artifact = Objects.requireNonNull(artifact);
	}

	public ArtifactLoader(byte[] jar, Optional<PluginDescription> description) {
		this.jar = jar;
		this.description = description;
	}

	public byte[] getJar() {
		return jar;
	}

	public Optional<PluginDescription> getDescription() {
		return description;
	}

	public CompletableFuture<ArtifactLoader> load() {
		if (repository == null)
			throw new IllegalStateException("Couldn't to load because of missing repository");

		return CompletableFuture.allOf(
				repository.getPluginDescription(artifact)
						.thenAccept(description -> this.description = description),
				readArtifact(null, "jar")
						.thenAccept(data -> this.jar = data))
				.thenApply(dummy -> this);
	}

	private CompletableFuture<byte[]> readArtifact(String classifier, String type) {
		return new ReadToMemoryProcessor(output -> repository.getRepository().downloadArtifact(artifact, classifier, type, output))
				.invoke();
	}

}
