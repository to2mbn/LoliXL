package org.to2mbn.lolixl.ui.bootstrap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.osgi.framework.FrameworkUtil;
import org.to2mbn.lolixl.ui.bootstrap.impl.UIBootstrapStageServiceImpl;

import java.util.Hashtable;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@Component
public class UIBootstrapApp extends Application {
	private static final Logger LOGGER = Logger.getLogger(UIBootstrapApp.class.getCanonicalName());

	@Activate
	public void active() {
		LOGGER.info("Current thread: " + Thread.currentThread().getName() + ", launching JavaFX");
		Executors.defaultThreadFactory().newThread(() -> {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			Application.launch(UIBootstrapApp.class);
		}).start();
	}

	@Deactivate
	public void deactive() {
		Platform.exit();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		LOGGER.info("Current thread: " + Thread.currentThread().getName() + ", publishing stage service");
		FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(UIBootstrapStageService.class,
				new UIBootstrapStageServiceImpl(primaryStage), new Hashtable<>());
	}
}
