package org.to2mbn.lolixl.plugin.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.to2mbn.lolixl.plugin.PluginDescription;
import org.to2mbn.lolixl.plugin.PluginRepository;
import org.to2mbn.lolixl.plugin.maven.ArtifactNotFoundException;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;
import org.to2mbn.lolixl.utils.AsyncUtils;
import org.xml.sax.InputSource;

abstract public class AbstractPluginRepository implements PluginRepository {

	private PluginDescriptionResolver descriptionResolver = new PluginDescriptionResolver();

	@Override
	public CompletableFuture<Optional<PluginDescription>> getPluginDescription(MavenArtifact artifact) {
		Objects.requireNonNull(artifact);
		return new ReadToMemoryProcessor(output -> getRepository().downloadArtifact(artifact, "lolixl-plugin", "xml", output))
				.invoke()
				.handle((data, exception) -> {
					if (exception == null) {
						try (Reader reader = new InputStreamReader(new ByteArrayInputStream(data), "UTF-8")) {
							return Optional.of(descriptionResolver.resolve(new InputSource(reader)));
						} catch (Exception e) {
							throw new IllegalArgumentException("${org.to2mbn.lolixl.plugin.badDescription}", e);
						}
					} else if (AsyncUtils.exceptionInstanceof(ArtifactNotFoundException.class, exception)) {
						return Optional.empty();
					} else {
						throw AsyncUtils.wrapWithCompletionException(exception);
					}
				});
	}

}
