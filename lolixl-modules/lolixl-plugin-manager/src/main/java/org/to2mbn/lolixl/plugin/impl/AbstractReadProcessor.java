package org.to2mbn.lolixl.plugin.impl;

import static java.lang.String.format;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.to2mbn.lolixl.utils.AsyncUtils;

abstract public class AbstractReadProcessor<RESULT> implements Supplier<WritableByteChannel> {

	private static final Logger LOGGER = Logger.getLogger(AbstractReadProcessor.class.getCanonicalName());

	private Function<Supplier<WritableByteChannel>, CompletableFuture<Void>> operation;
	private AtomicInteger openCount = new AtomicInteger();
	private WritableByteChannel lastChannel;

	public AbstractReadProcessor(Function<Supplier<WritableByteChannel>, CompletableFuture<Void>> operation) {
		this.operation = operation;
	}

	abstract protected WritableByteChannel openChannel() throws IOException;

	abstract protected RESULT onSuccess() throws IOException;

	abstract protected void onFailure() throws IOException;

	private void throwChannelNotClosedException() {
		RuntimeException ex = new IllegalStateException("The previous channel is not closed");

		LOGGER.log(Level.SEVERE, ex, () -> format(
				"[%s] channel #%d is not closed, but caller is trying to open another channel. For security, close the channel first, and then throw an exception.",
				this, openCount.get()));

		if (lastChannel != null) {
			try {
				lastChannel.close();
			} catch (IOException e) {
				ex.addSuppressed(e);
			}
		}

		throw ex;
	}

	@Override
	public WritableByteChannel get() {
		if (lastChannel != null && lastChannel.isOpen()) {
			// Illegal state
			throwChannelNotClosedException();
			throw new AssertionError("unreachable statement");
		} else {
			int idx = openCount.getAndIncrement();
			LOGGER.finer("[" + this + "] opening channel #" + idx);
			try {
				lastChannel = openChannel();
				return lastChannel;
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	public CompletableFuture<RESULT> invoke() {
		return operation.apply(this)
				.handle((dummy, exception) -> {
					if (exception != null) {
						try {
							onFailure();
						} catch (IOException e) {
							exception.addSuppressed(e);
						}
						throw AsyncUtils.wrapWithCompletionException(exception);
					} else {
						if (openCount.get() == 0) {
							throw new IllegalStateException("Download wasn't performed");
						}
						if (lastChannel.isOpen()) {
							throwChannelNotClosedException();
						}
						try {
							return onSuccess();
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					}
				});
	}

}
