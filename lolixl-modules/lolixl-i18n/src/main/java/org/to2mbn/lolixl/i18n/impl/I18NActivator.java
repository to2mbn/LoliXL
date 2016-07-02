package org.to2mbn.lolixl.i18n.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.to2mbn.lolixl.i18n.LocalizationService;
import org.to2mbn.lolixl.i18n.spi.LocalizationRegistry;

public class I18NActivator implements BundleActivator {

	public static LocalizationService publicService;

	private LocalizationServiceImpl service;

	@Override
	public void start(BundleContext context) throws Exception {
		service = new LocalizationServiceImpl();
		publicService = service;

		context.registerService(LocalizationService.class, service, null);
		context.registerService(LocalizationRegistry.class, service, null);

		context.addBundleListener(e -> service.checkBundleState(e.getBundle()));
	}

	@Override
	public void stop(BundleContext context) throws Exception {}

}
