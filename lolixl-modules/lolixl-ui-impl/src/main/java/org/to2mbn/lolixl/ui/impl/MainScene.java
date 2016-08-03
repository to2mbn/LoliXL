package org.to2mbn.lolixl.ui.impl;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.ui.impl.container.presenter.DefaultFramePresenter;
import org.to2mbn.lolixl.utils.AsyncUtils;
import org.to2mbn.lolixl.utils.DictionaryAdapter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

@Component
public class MainScene {

	private static final Logger LOGGER = Logger.getLogger(MainScene.class.getCanonicalName());

	public static final String PROPERTY_SCENE_ID = "org.to2mbn.lolixl.ui.impl.scene.id";
	public static final String MAIN_SCENE_ID = "org.to2mbn.lolixl.ui.impl.scene.main.id";

	@Reference(target = "(" + MainStage.PROPERTY_STAGE_ID + "=" + MainStage.MAIN_STAGE_ID + ")")
	private Stage stage;

	@Reference
	private DefaultFramePresenter defaultFramePresenter;

	private static final String DEFAULT_METRO_STYLE_SHEET = "/ui/css/metro.css";

	@Activate
	public void active(ComponentContext compCtx) throws InterruptedException, ExecutionException {
		Scene scene = AsyncUtils.asyncRun(() -> {

			LOGGER.fine("Creating main scene");
			Scene m_scene = new Scene(defaultFramePresenter.getView().shadowContainer);
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader()); // 防止StyleManager智障读不到CSS
			m_scene.getStylesheets().add(DEFAULT_METRO_STYLE_SHEET);
			stage.setScene(m_scene);
			stage.show();
			return m_scene;

		}, Platform::runLater).get();

		Map<String, Object> properties = new HashMap<>();
		properties.put(PROPERTY_SCENE_ID, MAIN_SCENE_ID);
		compCtx.getBundleContext().registerService(Scene.class, scene, new DictionaryAdapter<>(properties));
	}

}
