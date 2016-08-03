package org.to2mbn.lolixl.ui.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.utils.AsyncUtils;
import org.to2mbn.lolixl.utils.DictionaryAdapter;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

@Component
public class MainStage {

	private static final Logger LOGGER = Logger.getLogger(MainStage.class.getCanonicalName());

	public static final String PROPERTY_STAGE_ID = "org.to2mbn.lolixl.ui.impl.stage.id";
	public static final String MAIN_STAGE_ID = "org.to2mbn.lolixl.ui.impl.stage.main.id";

	@Activate
	public void active(ComponentContext compCtx) throws InterruptedException, ExecutionException {
		Stage stage = AsyncUtils.asyncRun(() -> {

			LOGGER.fine("Creating main stage");
			Stage m_stage = new Stage();
			m_stage.initStyle(StageStyle.UNDECORATED);
			return m_stage;

		}, Platform::runLater).get();

		Map<String, Object> properties = new HashMap<>();
		properties.put(PROPERTY_STAGE_ID, MAIN_STAGE_ID);
		compCtx.getBundleContext().registerService(Stage.class, stage, new DictionaryAdapter<>(properties));
	}

}
