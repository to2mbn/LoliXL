package org.to2mbn.lolixl.ui.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.utils.DictionaryAdapter;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

@Component
public class MainStage {

	public static final String PROPERTY_STAGE_ID = "org.to2mbn.lolixl.ui.impl.stage.id";
	public static final String MAIN_STAGE_ID = "org.to2mbn.lolixl.ui.impl.stage.main.id";

	private static final Logger LOGGER = Logger.getLogger(MainStage.class.getCanonicalName());

	@Activate
	public void active(ComponentContext compCtx) {
		LOGGER.info("Initializing JavaFX engine");
		new JFXPanel(); // init JavaFX engine
		Platform.runLater(() -> {
			Stage stage = new Stage();
			stage.initStyle(StageStyle.UNDECORATED);

			Map<String, Object> properties = new HashMap<>();
			compCtx.getBundleContext().registerService(Stage.class, stage, new DictionaryAdapter<>(properties));
		});
	}

}
