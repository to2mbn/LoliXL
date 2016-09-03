package org.to2mbn.lolixl.ui.impl.stage;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.ui.impl.pages.home.HomeFramePresenter;
import org.to2mbn.lolixl.ui.impl.pages.home.TitleBarPresenter;
import org.to2mbn.lolixl.utils.DictionaryAdapter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

@Component
public class MainScene {

	private static final Logger LOGGER = Logger.getLogger(MainScene.class.getCanonicalName());

	public static final String PROPERTY_SCENE_ID = "org.to2mbn.lolixl.ui.scene";
	public static final String MAIN_SCENE_ID = "org.to2mbn.lolixl.ui.scene.main";

	@Reference(target = "(" + MainStage.PROPERTY_STAGE_ID + "=" + MainStage.MAIN_STAGE_ID + ")")
	private Stage stage;

	@Reference
	private HomeFramePresenter defaultFramePresenter;

	@Reference
	private TitleBarPresenter titleBarPresenter;

	@Activate
	public void active(ComponentContext compCtx) throws InterruptedException, ExecutionException {
		Platform.runLater(() -> {
			LOGGER.fine("Creating main scene");
			WindowContainer container = new WindowContainer();
			container.initContent(defaultFramePresenter.getView().rootContainer);
			Scene scene = new Scene(container);
			scene.setFill(Color.TRANSPARENT);
			stage.setScene(scene);
			container.initStage(stage);
			container.setDraggable(titleBarPresenter.getView().rootContainer);
			stage.show();

			Map<String, Object> properties = new HashMap<>();
			properties.put(PROPERTY_SCENE_ID, MAIN_SCENE_ID);
			compCtx.getBundleContext().registerService(Scene.class, scene, new DictionaryAdapter<>(properties));
		});
	}

}
