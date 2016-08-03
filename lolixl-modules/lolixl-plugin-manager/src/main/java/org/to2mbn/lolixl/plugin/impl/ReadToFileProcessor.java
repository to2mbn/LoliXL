package org.to2mbn.lolixl.plugin.impl;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import org.to2mbn.lolixl.utils.PathUtils;

public class ReadToFileProcessor extends AbstractReadProcessor<Void> {

	static final Logger LOGGER = Logger.getLogger(ReadToFileProcessor.class.getCanonicalName());

	private Path to;
	private Path toTemp;

	public ReadToFileProcessor(Path to, Function<Supplier<WritableByteChannel>, CompletableFuture<Void>> operation) {
		super(operation);
		this.to = Objects.requireNonNull(to);

		Path parent = to.getParent();
		if (parent == null) {
			toTemp = to.getFileSystem().getPath(to.getFileName() + ".part");
		} else {
			toTemp = parent.resolve(to.getFileName() + ".part");
		}
	}

	@Override
	protected WritableByteChannel openChannel() throws IOException {
		Files.deleteIfExists(toTemp);
		PathUtils.tryMkdirsParent(toTemp);
		return FileChannel.open(toTemp, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
	}

	@Override
	protected Void onSuccess() throws IOException {
		try {
			Files.move(toTemp, to, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException exCopy) {
			try {
				Files.deleteIfExists(toTemp);
			} catch (IOException e) {
				exCopy.addSuppressed(e);
			}
			throw exCopy;
		}
		return null;
	}

	@Override
	protected void onFailure() throws IOException {
		Files.deleteIfExists(toTemp);
	}

	@Override
	public String toString() {
		return "[to=" + to + "@" + System.identityHashCode(this) + "]";
	}

}
