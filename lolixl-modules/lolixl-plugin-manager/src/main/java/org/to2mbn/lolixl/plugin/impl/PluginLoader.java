package org.to2mbn.lolixl.plugin.impl;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.to2mbn.lolixl.plugin.gpg.GPGVerifier;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;
import org.to2mbn.lolixl.plugin.maven.MavenRepository;

public class PluginLoader {

	private byte[] jar;
	private PluginDescriptionImpl description;

	private MavenRepository repository;
	private MavenArtifact artifact;
	private GPGVerifier verifier;

	public PluginLoader(GPGVerifier verifier, MavenRepository repository, MavenArtifact artifact) {
		this.verifier = Objects.requireNonNull(verifier);
		this.repository = Objects.requireNonNull(repository);
		this.artifact = Objects.requireNonNull(artifact);
	}

	public byte[] getJar() {
		return jar;
	}

	public PluginDescriptionImpl getDescription() {
		return description;
	}

	public MavenRepository getRepository() {
		return repository;
	}

	public MavenArtifact getArtifact() {
		return artifact;
	}

	public CompletableFuture<PluginLoader> load() {
		return CompletableFuture.allOf(
				readSignedArtifact(null, "jar"),
				readSignedArtifact("lolixl-plugin", "xml"))
				.thenApply(dummy -> this);
	}

	private CompletableFuture<byte[]> readSignedArtifact(String classifier, String type) {
		return readArtifact(classifier, type)
				.thenCombine(readArtifact(classifier, type + ".asc"),
						(data, signature) -> verifier.verify(data, signature))
				.thenCompose(f -> f);
	}

	private CompletableFuture<byte[]> readArtifact(String classifier, String type) {
		return new ReadToMemoryProcessor(output -> repository.downloadArtifact(artifact, classifier, type, output))
				.invoke();
	}

}
