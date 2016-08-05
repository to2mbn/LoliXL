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

	@Reference
	private EventAdmin eventAdmin;

	@Activate
	public void active(ComponentContext compCtx) throws InterruptedException, ExecutionException {
		Platform.runLater(() -> {

			LOGGER.fine("Creating main stage");
			Stage stage = new Stage();
			stage.setOnCloseRequest(event -> eventAdmin.postEvent(new ApplicationExitEvent()));

			Map<String, Object> properties = new HashMap<>();
			properties.put(PROPERTY_STAGE_ID, MAIN_STAGE_ID);
			compCtx.getBundleContext().registerService(Stage.class, stage, new DictionaryAdapter<>(properties));

		});
	}

}
