package org.to2mbn.lolixl.core.impl.ehcache;

import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.osgi.service.component.ComponentContext;

@Component
public class EhcacheFactory {

	private static final Logger LOGGER = Logger.getLogger(EhcacheFactory.class.getCanonicalName());

	private CacheManager cacheManager;

	@Activate
	public void active(ComponentContext compCtx) {
		new Thread(() -> {
			LOGGER.info("Starting Ehcache");
			cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
					.with(CacheManagerBuilder.persistence(".lolixl/ehcache"))
					.withCache("org.to2mbn.lolixl.core.impl.texture.binary",
							CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, byte[].class,
									ResourcePoolsBuilder.newResourcePoolsBuilder()
											.disk(16, MemoryUnit.MB, true)))
					.build(true);
			compCtx.getBundleContext().registerService(CacheManager.class, cacheManager, null);
		}, "Ehcache-init").start();
	}

	@Deactivate
	public void deactive() {
		LOGGER.info("Stopping Ehcache");
		cacheManager.close();
	}

}
