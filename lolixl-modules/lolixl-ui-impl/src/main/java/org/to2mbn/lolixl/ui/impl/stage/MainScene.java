package org.to2mbn.lolixl.ui.impl.stage;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.to2mbn.lolixl.ui.event.CssApplyEvent;
import org.to2mbn.lolixl.ui.impl.pages.home.HomeFramePresenter;
import org.to2mbn.lolixl.ui.impl.pages.home.TitleBarPresenter;
import org.to2mbn.lolixl.utils.DictionaryAdapter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

@Service({ EventHandler.class })
@Properties({
		@Property(name = EventConstants.EVENT_TOPIC, value = CssApplyEvent.TOPIC_CSS_APPLY)
})
@Component(immediate = true)
public class MainScene implements EventHandler {

	private static final Logger LOGGER = Logger.getLogger(MainScene.class.getCanonicalName());

	public static final String PROPERTY_SCENE_ID = "org.to2mbn.lolixl.ui.scene";
	public static final String MAIN_SCENE_ID = "org.to2mbn.lolixl.ui.scene.main";

	@Reference(target = "(" + MainStage.PROPERTY_STAGE_ID + "=" + MainStage.MAIN_STAGE_ID + ")")
	private Stage stage;

	@Reference
	private HomeFramePresenter defaultFramePresenter;

	@Reference
	private TitleBarPresenter titleBarPresenter;

	private Scene scene;
	private BundleContext bundleCtx;

	@Activate
	public void active(ComponentContext compCtx) throws InterruptedException, ExecutionException {
		bundleCtx = compCtx.getBundleContext();
		Platform.runLater(() -> {
			LOGGER.fine("Creating main scene");
			WindowContainer container = new WindowContainer();
			container.initContent(defaultFramePresenter.getView().rootContainer);
			scene = new Scene(container);
			scene.setFill(Color.TRANSPARENT);
			stage.setScene(scene);
			container.initStage(stage);
			container.setDraggable(titleBarPresenter.getView().rootContainer);

			Map<String, Object> properties = new HashMap<>();
			properties.put(PROPERTY_SCENE_ID, MAIN_SCENE_ID);
			bundleCtx.registerService(Scene.class, scene, new DictionaryAdapter<>(properties));
		});
	}

	@Override
	public void handleEvent(Event event) {
		if (event instanceof CssApplyEvent && ((CssApplyEvent) event).getPlugin().getBundle() == bundleCtx.getBundle()) {
			Platform.runLater(() -> {
				stage.show();
				Platform.runLater(() -> {
					stage.setResizable(false);
					defaultFramePresenter.updateAreaPosition();
				});
			});
		}
	}

}
