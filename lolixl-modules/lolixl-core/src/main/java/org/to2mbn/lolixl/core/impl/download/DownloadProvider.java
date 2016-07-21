package org.to2mbn.lolixl.core.impl.download;

import java.util.Collections;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventAdmin;
import org.to2mbn.jmccc.mcdownloader.MinecraftDownloader;
import org.to2mbn.jmccc.mcdownloader.MinecraftDownloaderBuilder;
import org.to2mbn.jmccc.mcdownloader.download.Downloader;
import org.to2mbn.jmccc.mcdownloader.download.DownloaderBuilders;
import org.to2mbn.jmccc.mcdownloader.download.combine.CombinedDownloader;
import org.to2mbn.jmccc.mcdownloader.provider.DownloadProviderChain;
import org.to2mbn.jmccc.util.Builder;

@Component
public class DownloadProvider {

	@Reference
	private EventAdmin eventAdmin;

	private MinecraftDownloader downloader;
	private OSGiDownloadProviderAdapter downloadProviderAdapter;
	private OSGiDownloadInfoProcessorAdapter downloadInfoProcessorAdapter;

	@Activate
	public void active(ComponentContext compCtx) {
		BundleContext ctx = compCtx.getBundleContext();
		downloadInfoProcessorAdapter = new OSGiDownloadInfoProcessorAdapter(ctx);
		downloadProviderAdapter = new OSGiDownloadProviderAdapter(ctx, () -> Collections.emptyList(), downloadInfoProcessorAdapter);

		downloader = MinecraftDownloaderBuilder.create(
				DownloaderBuilders.cacheableDownloader(
						wrapDownloader(
								DownloaderBuilders.downloader())))
				.providerChain(DownloadProviderChain.create()
						.addProvider(downloadProviderAdapter.getDownloadProvider())
						.addDownloadInfoProcessor(downloadInfoProcessorAdapter))
				.build();

		ctx.registerService(MinecraftDownloader.class, downloader, null);
		ctx.registerService(CombinedDownloader.class, downloader, null);
		ctx.registerService(Downloader.class, downloader, null);
	}

	@Deactivate
	public void deactive() {
		downloader.shutdown();
		downloadProviderAdapter.close();
		downloadInfoProcessorAdapter.close();
	}

	private Builder<Downloader> wrapDownloader(Builder<Downloader> wrapped) {
		return () -> new DownloaderWrapper(wrapped.build(), eventAdmin);
	}

}
