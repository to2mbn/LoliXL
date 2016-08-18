package org.to2mbn.lolixl.ui.impl;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventAdmin;
import org.to2mbn.lolixl.utils.DictionaryAdapter;
import org.to2mbn.lolixl.utils.event.ApplicationExitEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

@Component
public class MainStage {

	private static final Logger LOGGER = Logger.getLogger(MainStage.class.getCanonicalName());

	public static final String PROPERTY_STAGE_ID = "org.to2mbn.lolixl.ui.impl.stage.id";
	public static final String MAIN_STAGE_ID = "org.to2mbn.lolixl.ui.impl.stage.main.id";

	private static final double WIDTH = 850.0;
	private static final double HEIGHT = 450.0;
	private static final double MIN_WIDTH = 600.0;
	private static final double MIN_HEIGHT = 340.0;

	@Reference
	private EventAdmin eventAdmin;

	@Activate
	public void active(ComponentContext compCtx) throws InterruptedException, ExecutionException {
		Platform.runLater(() -> {

			LOGGER.fine("Creating main stage");
			Stage stage = new Stage();
			stage.setOnCloseRequest(event -> eventAdmin.postEvent(new ApplicationExitEvent()));
			stage.setWidth(WIDTH);
			stage.setHeight(HEIGHT);
			stage.setMinWidth(MIN_WIDTH);
			stage.setMinHeight(MIN_HEIGHT);

			Map<String, Object> properties = new HashMap<>();
			properties.put(PROPERTY_STAGE_ID, MAIN_STAGE_ID);
			compCtx.getBundleContext().registerService(Stage.class, stage, new DictionaryAdapter<>(properties));

		});
	}

}
