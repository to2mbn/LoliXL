package org.to2mbn.lolixl.plugin.gpg;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface GPGKeyProvider {

	CompletableFuture<Optional<byte[]>> getPublicKey(long keyId);

}
