package org.to2mbn.lolixl.init;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.to2mbn.lolixl.utils.event.ApplicationExitEvent;
import javafx.application.Platform;

@Service({ EventHandler.class })
@Properties({
		@Property(name = EventConstants.EVENT_TOPIC, value = ApplicationExitEvent.TOPIC_APPLICATION_EXIT),
		@Property(name = Constants.SERVICE_RANKING, intValue = Integer.MIN_VALUE)
})
@Component(immediate = true)
public class ApplicationExitHandler implements EventHandler {

	private static final Logger LOGGER = Logger.getLogger(ApplicationExitHandler.class.getCanonicalName());

	private long stopWaitTime = 3000;

	private BundleContext bundleContext;

	@Activate
	public void active(ComponentContext compCtx) {
		bundleContext = compCtx.getBundleContext();
	}

	@Override
	public void handleEvent(Event event) {
		LOGGER.info("Exiting application");

		stopFx();
		stopBundles();
		stopBundles(); // some bundles may be started during last stopBundles() invocation

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

	private void stopBundles() {
		for (Bundle bundle : bundleContext.getBundles()) {
			if (bundle.getState() == Bundle.ACTIVE ||
					bundle.getState() == Bundle.STARTING) {
				LOGGER.fine("Try to stop bundle " + bundle);
				try {
					bundle.stop();
				} catch (Throwable e) {
					LOGGER.log(Level.SEVERE, "Couldn't stop bundle " + bundle, e);
				}
			}
		}
	}

	private void stopFx() {
		Platform.exit();
	}

}
