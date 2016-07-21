package org.to2mbn.lolixl.plugin.impl;

import static java.lang.String.format;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.to2mbn.lolixl.utils.PathUtils;

public class ReadToFileProcessor implements Supplier<WritableByteChannel> {

	private static final Logger LOGGER = Logger.getLogger(ReadToFileProcessor.class.getCanonicalName());

	private Path to;
	private Path toTemp;
	private Function<Supplier<WritableByteChannel>, CompletableFuture<Void>> operation;

	private AtomicInteger openCount = new AtomicInteger();
	private AtomicBoolean channelOpened = new AtomicBoolean();
	private FileChannel lastChannel;

	public ReadToFileProcessor(Path to, Function<Supplier<WritableByteChannel>, CompletableFuture<Void>> operation) {
		this.to = Objects.requireNonNull(to);
		this.operation = Objects.requireNonNull(operation);

		Path parent = to.getParent();
		if (parent == null) {
			toTemp = to.getFileSystem().getPath(to.getFileName() + ".part");
		} else {
			toTemp = parent.resolve(to.getFileName() + ".part");
		}
	}

	public CompletableFuture<Void> invoke() {
		return operation.apply(this)
				.whenComplete((dummy, exception) -> {
					if (exception != null) {
						try {
							Files.deleteIfExists(toTemp);
						} catch (IOException e) {
							exception.addSuppressed(e);
						}
					} else {
						if (openCount.get() == 0) {
							throw new IllegalStateException("Download wasn't performed");
						}
						try {
							Files.move(toTemp, to, StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException exCopy) {
							try {
								Files.deleteIfExists(toTemp);
							} catch (IOException e) {
								exCopy.addSuppressed(e);
							}
							throw new UncheckedIOException(exCopy);
						}
					}
				});
	}

	@Override
	public WritableByteChannel get() {
		if (!channelOpened.compareAndSet(false, true)) {
			// Illegal state
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

		} else {
			int idx = openCount.getAndIncrement();
			LOGGER.finer(() -> format("[%s] opening channel #%d", this, idx));
			try {
				Files.deleteIfExists(toTemp);
				PathUtils.tryMkdirsParent(toTemp);
				lastChannel = FileChannel.open(toTemp, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
				return lastChannel;
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	@Override
	public String toString() {
		return "[to=" + to + "@" + System.identityHashCode(this) + "]";
	}

}
