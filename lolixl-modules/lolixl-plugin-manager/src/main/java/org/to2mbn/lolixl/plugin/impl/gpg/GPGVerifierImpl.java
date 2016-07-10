package org.to2mbn.lolixl.plugin.impl.gpg;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.SignatureException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.to2mbn.lolixl.plugin.gpg.GPGKeyProvider;
import org.to2mbn.lolixl.plugin.gpg.GPGVerifier;
import org.to2mbn.lolixl.utils.AsyncUtils;

@Service({ GPGVerifier.class })
@Component
public class GPGVerifierImpl implements GPGVerifier {

	@Reference(target = "(usage=cpu_compute)")
	private ExecutorService cpuComputePool;

	private ServiceTracker<GPGKeyProvider, GPGKeyProvider> serviceTracker;

	@Activate
	public void active(ComponentContext compCtx) {
		serviceTracker = new ServiceTracker<>(compCtx.getBundleContext(), GPGKeyProvider.class, null);
		serviceTracker.open();
	}

	@Deactivate
	public void deactive() {
		serviceTracker.close();
	}

	private CompletableFuture<byte[]> getPublicKey(GPGKeyProvider[] providers, long keyId, int idx) {
		if (idx >= providers.length) {
			CompletableFuture<byte[]> future = new CompletableFuture<>();
			future.completeExceptionally(new PGPException("${org.to2mbn.lolixl.gpg.keyProvider.noMoreToTry}"));
			return future;
		}

		return providers[idx].getPublicKey(keyId)
				.thenCompose(result -> {
					if (result.isPresent()) {
						return CompletableFuture.supplyAsync(() -> result.get());
					} else {
						return getPublicKey(providers, keyId, idx + 1);
					}
				});
	}

	@Override
	public CompletableFuture<byte[]> verify(byte[] dataBuf, byte[] signatureBuf) {
		return AsyncUtils.asyncRun(() -> {
			InputStream signatureIn = PGPUtil.getDecoderStream(new ByteArrayInputStream(signatureBuf));
			JcaPGPObjectFactory pgpFact = new JcaPGPObjectFactory(signatureIn);
			PGPSignatureList p3;

			Object o = pgpFact.nextObject();
			if (o instanceof PGPCompressedData) {
				PGPCompressedData c1 = (PGPCompressedData) o;
				pgpFact = new JcaPGPObjectFactory(c1.getDataStream());
				p3 = (PGPSignatureList) pgpFact.nextObject();
			} else {
				p3 = (PGPSignatureList) o;
			}
			return p3.get(0);
		}, cpuComputePool)
				.thenCompose(signature -> getPublicKey(serviceTracker.getServices(new GPGKeyProvider[0]), signature.getKeyID(), 0)
						.thenCompose(keyBuf -> AsyncUtils.asyncRun(() -> {
							PGPPublicKeyRingCollection pgpPubRingCollection = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(new ByteArrayInputStream(keyBuf)), new JcaKeyFingerprintCalculator());
							PGPPublicKey key = pgpPubRingCollection.getPublicKey(signature.getKeyID());
							signature.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), key);
							InputStream dIn = new ByteArrayInputStream(dataBuf);
							int ch;
							while ((ch = dIn.read()) >= 0) {
								signature.update((byte) ch);
							}
							if (signature.verify()) {
								// TODO: Ask user to accept the public key
								return dataBuf;
							} else {
								throw new SignatureException("${org.to2mbn.lolixl.gpg.invalidSignature}");
							}
						}, cpuComputePool)));
	}

}
