package org.to2mbn.lolixl.plugin.impl;

import static java.lang.String.format;
import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReadToMemoryProcessor implements Supplier<WritableByteChannel> {

	private static final Logger LOGGER = Logger.getLogger(ReadToMemoryProcessor.class.getCanonicalName());

	private Function<Supplier<WritableByteChannel>, CompletableFuture<Void>> operation;

	private ByteArrayOutputStream buf;
	private WritableByteChannel channel;
	private AtomicInteger openCount = new AtomicInteger();
	private AtomicBoolean channelOpened = new AtomicBoolean();

	public ReadToMemoryProcessor(Function<Supplier<WritableByteChannel>, CompletableFuture<Void>> operation) {
		this.operation = operation;
	}

	public CompletableFuture<byte[]> invoke() {
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
		if (!channelOpened.compareAndSet(false, true)) {
			// Illegal state
			RuntimeException ex = new IllegalStateException("The previous channel is not closed");

			LOGGER.log(Level.SEVERE, ex, () -> format(
					"[%s] channel #%d is not closed, but caller is trying to open another channel. For security, throw an exception.",
					this, openCount.get()));

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
