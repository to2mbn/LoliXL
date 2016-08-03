package org.to2mbn.lolixl.plugin.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public class ReadToMemoryProcessor extends AbstractReadProcessor<byte[]> {

	private ByteArrayOutputStream buf;

	public ReadToMemoryProcessor(Function<Supplier<WritableByteChannel>, CompletableFuture<Void>> operation) {
		super(operation);
	}

	@Override
	protected WritableByteChannel openChannel() throws IOException {
		buf = new ByteArrayOutputStream();
		return Channels.newChannel(buf);
	}

	@Override
	protected byte[] onSuccess() throws IOException {
		return buf.toByteArray();
	}

	@Override
	protected void onFailure() throws IOException {}

}
