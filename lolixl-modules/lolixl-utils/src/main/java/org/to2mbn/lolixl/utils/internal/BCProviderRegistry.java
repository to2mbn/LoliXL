package org.to2mbn.lolixl.utils.internal;

// import java.security.Security;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
// import org.bouncycastle.jce.provider.BouncyCastleProvider;

@Component(enabled = false)
public class BCProviderRegistry {

	@Activate
	public void active() {
		// Security.addProvider(new BouncyCastleProvider());
	}

}
