package org.to2mbn.lolixl.core.impl.download;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.function.Supplier;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.to2mbn.jmccc.mcdownloader.provider.DownloadInfoProcessor;
import org.to2mbn.jmccc.mcdownloader.provider.DownloadProviderChain;
import org.to2mbn.jmccc.mcdownloader.provider.ExtendedDownloadProvider;
import org.to2mbn.jmccc.mcdownloader.provider.MinecraftDownloadProvider;
import org.to2mbn.jmccc.util.Builder;
import org.to2mbn.lolixl.utils.LambdaServiceTracker;

public class OSGiDownloadProviderAdapter {

	private Supplier<List<Builder<MinecraftDownloadProvider>>> headBuilder;
	private DownloadInfoProcessor downloadInfoProcessor;

	private LambdaServiceTracker<MinecraftDownloadProvider> serviceTracker;
	private MinecraftDownloadProvider downloadProvider;

	private MinecraftDownloadProvider jmcccUpstream;

	public OSGiDownloadProviderAdapter(BundleContext ctx, Supplier<List<Builder<MinecraftDownloadProvider>>> headBuilder, DownloadInfoProcessor downloadInfoProcessor) {
		this.headBuilder = headBuilder;
		this.downloadInfoProcessor = downloadInfoProcessor;
		this.serviceTracker = new LambdaServiceTracker<>(ctx, MinecraftDownloadProvider.class)
				.whenAdding((reference, service) -> initProvider(reference, service))
				.whenModifying((reference, service) -> initProvider(reference, service));
		serviceTracker.open(true);

		downloadProvider = (MinecraftDownloadProvider) Proxy.newProxyInstance(OSGiDownloadProviderAdapter.class.getClassLoader(), new Class<?>[] { ExtendedDownloadProvider.class, MinecraftDownloadProvider.class },
				(proxy, method, args) -> {
					if ("setUpstreamProvider".equals(method.getName()) &&
							void.class.equals(method.getReturnType())) {
						Class<?>[] methodargs = method.getParameterTypes();
						if (methodargs.length == 1 &&
								MinecraftDownloadProvider.class.equals(methodargs[0])) {
							jmcccUpstream = (MinecraftDownloadProvider) args[0];
							return null;
						}
					}

					for (MinecraftDownloadProvider subProvider : serviceTracker.getServices(new MinecraftDownloadProvider[0])) {
						Object result = method.invoke(subProvider, args);
						if (result != null) {
							return result;
						}
					}

					return null;
				});
	}

	private void initProvider(ServiceReference<MinecraftDownloadProvider> reference, MinecraftDownloadProvider service) {
		if (service instanceof ExtendedDownloadProvider)
			((ExtendedDownloadProvider) service).setUpstreamProvider(createUpstream(reference, service));
	}

	private MinecraftDownloadProvider createUpstream(ServiceReference<MinecraftDownloadProvider> reference, MinecraftDownloadProvider service) {
		return (MinecraftDownloadProvider) Proxy.newProxyInstance(OSGiDownloadProviderAdapter.class.getClassLoader(), new Class<?>[] { MinecraftDownloadProvider.class },
				(proxy, method, args) -> {
					MinecraftDownloadProvider[] allProviders = serviceTracker.getServices(new MinecraftDownloadProvider[0]);
					int idx = -1;
					for (int i = 0; i < allProviders.length; i++) {
						if (allProviders[i] == service) {
							idx = i;
							break;
						}
					}
					DownloadProviderChain chain = DownloadProviderChain.create();
					headBuilder.get().forEach(chain::addAheadProvider);
					for (int i = idx + 1; i < allProviders.length; i++)
						chain.addProvider(allProviders[i]);
					chain.baseProvider(jmcccUpstream);
					chain.addDownloadInfoProcessor(downloadInfoProcessor);
					MinecraftDownloadProvider handler = chain.build();
					return method.invoke(handler, args);
				});
	}

	public void close() {
		serviceTracker.close();
	}

	public MinecraftDownloadProvider getDownloadProvider() {
		return downloadProvider;
	}

}
