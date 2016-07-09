package org.to2mbn.lolixl.plugin.impl;

import static java.lang.String.format;
import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Reference;
import org.to2mbn.lolixl.plugin.PluginDescription;
import org.to2mbn.lolixl.plugin.PluginRepository;
import org.to2mbn.lolixl.plugin.maven.ArtifactNotFoundException;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;
import org.xml.sax.InputSource;

abstract public class AbstractPluginRepository implements PluginRepository {

	private static final Logger LOGGER = Logger.getLogger(AbstractPluginRepository.class.getCanonicalName());

	private static class MemoryDownloadProcessor implements Supplier<WritableByteChannel> {

		private ByteArrayOutputStream buf;
		private WritableByteChannel channel;
		private AtomicInteger openCount = new AtomicInteger();
		private AtomicBoolean channelOpened = new AtomicBoolean();

		public CompletableFuture<byte[]> invoke(Function<Supplier<WritableByteChannel>, CompletableFuture<Void>> operation) {
			return operation.apply(this)
					.whenComplete((dummy, exception) -> {
						if (exception == null && openCount.get() == 0) {
							throw new IllegalStateException("Download wasn't performed");
						}
					})
					.thenApply(dummy -> buf.toByteArray());
		}

		@Override
		public WritableByteChannel get() {
			if (channelOpened.compareAndSet(false, true)) {
				// Illegal state
				RuntimeException ex = new IllegalStateException("The previous channel is not closed");

				LOGGER.log(Level.SEVERE, ex, () -> format(
						"[%s] channel #%d is not closed, but caller is trying to open another channel. For security, throw an exception.",
						this, openCount));

				throw ex;

			} else {
				int idx = openCount.getAndIncrement();
				LOGGER.finer(() -> format("[%s] opening channel #%d", this, idx));
				buf = new ByteArrayOutputStream();
				channel = Channels.newChannel(buf);
				return channel;
			}
		}

	}

	@Reference
	private PluginDescriptionResolver descriptionResolver;

	@Override
	public CompletableFuture<Optional<PluginDescription>> getPluginDescription(MavenArtifact artifact) {
		Objects.requireNonNull(artifact);
		return new MemoryDownloadProcessor().invoke(
				output -> getRepository().downloadArtifact(artifact, "lolixl-plugin", "xml", output))
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
