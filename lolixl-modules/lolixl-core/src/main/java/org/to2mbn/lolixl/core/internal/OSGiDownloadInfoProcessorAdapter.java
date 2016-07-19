package org.to2mbn.lolixl.core.internal;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.to2mbn.jmccc.mcdownloader.provider.DownloadInfoProcessor;

public class OSGiDownloadInfoProcessorAdapter implements DownloadInfoProcessor {

	private ServiceTracker<DownloadInfoProcessor, DownloadInfoProcessor> serviceTracker;

	public OSGiDownloadInfoProcessorAdapter(BundleContext ctx) {
		serviceTracker = new ServiceTracker<>(ctx, DownloadInfoProcessor.class, null);
		serviceTracker.open(true);
	}

	public void close() {
		serviceTracker.close();
	}

	@Override
	public String process(String uri) {
		for (DownloadInfoProcessor processor : serviceTracker.getServices(new DownloadInfoProcessor[0])) {
			uri = processor.process(uri);
		}
		return uri;
	}

}
