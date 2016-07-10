package org.to2mbn.lolixl.plugin.impl;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.apache.felix.scr.annotations.Reference;
import org.to2mbn.lolixl.plugin.PluginDescription;
import org.to2mbn.lolixl.plugin.PluginRepository;
import org.to2mbn.lolixl.plugin.maven.ArtifactNotFoundException;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;
import org.xml.sax.InputSource;

abstract public class AbstractPluginRepository implements PluginRepository {

	@Reference
	private PluginDescriptionResolver descriptionResolver;

	@Override
	public CompletableFuture<Optional<PluginDescription>> getPluginDescription(MavenArtifact artifact) {
		Objects.requireNonNull(artifact);
		return new ReadToMemoryProcessor(output -> getRepository().downloadArtifact(artifact, "lolixl-plugin", "xml", output))
				.invoke()
				.handle((data, exception) -> {
					if (exception == null) {
						try {
							return Optional.of(descriptionResolver.resolve(new InputSource(new String(data, "UTF-8"))));
						} catch (Exception e) {
							throw new IllegalArgumentException("${org.to2mbn.lolixl.plugin.badDescription}", e);
						}
					} else if (exception instanceof ArtifactNotFoundException) {
						return Optional.empty();
					} else {
						throw new RuntimeException(exception);
					}
				});
	}

}
