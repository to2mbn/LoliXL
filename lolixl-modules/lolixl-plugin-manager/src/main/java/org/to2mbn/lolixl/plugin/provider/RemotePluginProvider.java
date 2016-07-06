package org.to2mbn.lolixl.plugin.provider;

import java.nio.channels.ReadableByteChannel;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.to2mbn.lolixl.plugin.MavenArtifact;

public interface RemotePluginProvider {

	CompletableFuture<Optional<ReadableByteChannel>> downloadArtifact(MavenArtifact artifact, String classifier, String type);

}
