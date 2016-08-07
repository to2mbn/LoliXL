package org.to2mbn.lolixl.core.impl.ehcache;

import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.osgi.service.component.ComponentContext;

@Component
public class EhcacheFactory {

	private static final Logger LOGGER = Logger.getLogger(EhcacheFactory.class.getCanonicalName());

	private CacheManager cacheManager;

	@Activate
	public void active(ComponentContext compCtx) {
		LOGGER.info("Starting Ehcache");
		cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
				.with(CacheManagerBuilder.persistence(".lolixl/ehcache"))
				.build(true);
		compCtx.getBundleContext().registerService(CacheManager.class, cacheManager, null);
	}

	@Deactivate
	public void deactive() {
		LOGGER.info("Stopping Ehcache");
		cacheManager.close();
	}

}
