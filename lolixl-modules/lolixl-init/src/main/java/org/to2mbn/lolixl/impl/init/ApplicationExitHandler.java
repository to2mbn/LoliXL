package org.to2mbn.lolixl.impl.init;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.to2mbn.lolixl.utils.event.ApplicationExitEvent;

@Service({ EventHandler.class })
@Properties({
		@Property(name = EventConstants.EVENT_TOPIC, value = ApplicationExitEvent.TOPIC_APPLICATION_EXIT),
		@Property(name = Constants.SERVICE_RANKING, intValue = Integer.MIN_VALUE)
})
@Component(immediate = true)
public class ApplicationExitHandler implements EventHandler {

	private static final Logger LOGGER = Logger.getLogger(ApplicationExitHandler.class.getCanonicalName());

	private long stopWaitTime = 5000;

	private BundleContext bundleContext;

	@Activate
	public void active(ComponentContext compCtx) {
		bundleContext = compCtx.getBundleContext();
	}

	@Override
	public void handleEvent(Event event) {
		LOGGER.info("Exiting application");

		stopFramework();

		if ("true".equals(System.getProperty("lolixl.forciblyExit"))) {
			Thread t = new Thread(() -> {
				try {
					Thread.sleep(stopWaitTime);
				} catch (InterruptedException e) {
					// ignore
				}
				LOGGER.warning("Application hasn't exited during last " + stopWaitTime + "ms, invoking Systen.exit()");
				System.exit(0);
			});
			t.setName("Application Killer");
			t.setDaemon(true);
			t.start();
		}
	}

	private void stopFramework() {
		try {
			bundleContext.getBundle(0).stop();
		} catch (BundleException e) {
			LOGGER.log(Level.SEVERE, "Couldn't stop framework", e);
		}
	}

}
