package org.to2mbn.lolixl.ui.impl;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.to2mbn.lolixl.ui.PresenterManagementService;
import org.to2mbn.lolixl.ui.event.UIInitializationEvent;
import org.to2mbn.lolixl.ui.event.UIPostInitializationEvent;
import org.to2mbn.lolixl.ui.impl.container.presenter.DefaultFramePresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.DefaultSideBarPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.DefaultTitleBarPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.HomeContentPresenter;
import java.util.logging.Logger;

@Component(immediate = true)
public class UIApp {
	private static final Logger LOGGER = Logger.getLogger(UIApp.class.getCanonicalName());

	private static final String DEFAULT_METRO_STYLE_SHEET = "/ui/css/metro.css";

	@Reference
	private EventAdmin eventAdmin;

	@Reference
	private PresenterManagementService presenterService;

	private static final ReadOnlyObjectWrapper<Stage> mainStageProperty = new ReadOnlyObjectWrapper<>(null);
	private static final ReadOnlyObjectWrapper<Scene> mainSceneProperty = new ReadOnlyObjectWrapper<>(null);

	@Activate
	public void active(ComponentContext compCtx) {
		LOGGER.info("Initializing JavaFX engine");
		new JFXPanel(); // init JavaFX engine
		Platform.runLater(() -> start(new Stage()));
	}

	private void start(Stage mainStage) {
		LOGGER.info("Initializing window");
		mainStageProperty.set(mainStage);
		mainStage.initStyle(StageStyle.UNDECORATED);
		fireEvent(new UIInitializationEvent());

		Scene mainScene = new Scene(presenterService.getPresenter(DefaultFramePresenter.class).getView().rootContainer);
		mainSceneProperty.set(mainScene);
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader()); // 防止StyleManager智障读不到CSS
		mainScene.getStylesheets().add(DEFAULT_METRO_STYLE_SHEET);
		mainStage.setScene(mainScene);
		fireEvent(new UIPostInitializationEvent());

		initLayout();
		mainStage.show();
	}

	private void initLayout() {
		LOGGER.info("Initializing layout");
		DefaultFramePresenter presenter = presenterService.getPresenter(DefaultFramePresenter.class);
		presenter.setTitleBar(presenterService.getPresenter(DefaultTitleBarPresenter.class).getView().rootContainer);
		presenter.setSidebar(presenterService.getPresenter(DefaultSideBarPresenter.class).getView().rootContainer);
		presenter.setContent(presenterService.getPresenter(HomeContentPresenter.class).getView().rootContainer);
	}

	private void fireEvent(Event event) {
		eventAdmin.postEvent(event);
	}

	public static ReadOnlyObjectProperty<Stage> mainStageProperty() {
		return mainStageProperty.getReadOnlyProperty();
	}

	public static ReadOnlyObjectProperty<Scene> mainSceneProperty() {
		return mainSceneProperty.getReadOnlyProperty();
	}
}
