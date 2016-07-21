package org.to2mbn.lolixl.ui.impl;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventAdmin;
import org.to2mbn.lolixl.ui.BackgroundService;
import org.to2mbn.lolixl.ui.PanelDisplayService;
import org.to2mbn.lolixl.ui.TileManagingService;
import org.to2mbn.lolixl.ui.impl.container.presenter.DefaultFramePresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.DefaultSideBarPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.DefaultTitleBarPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.content.HomeContentPresenter;
import org.to2mbn.lolixl.utils.event.ApplicationExitEvent;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.logging.Logger;

@Component
public class UIApp {

	private static final Logger LOGGER = Logger.getLogger(UIApp.class.getCanonicalName());

	private static final String[] LOCATIONS_OF_DEFAULT_CSS = {"/ui/css/metro.css", "/ui/css/components.css"};

	@Reference
	private EventAdmin eventAdmin;

	private Stage mainStage;
	private Scene mainScene;

	private DefaultFramePresenter framePresenter;
	private DefaultTitleBarPresenter titleBarPresenter;
	private DefaultSideBarPresenter sideBarPresenter;
	private HomeContentPresenter homeContentPresenter;

	private PanelDisplayService displayService;

	@Activate
	public void active(ComponentContext compCtx) {
		LOGGER.info("Initializing UI");

		// Create presenters
		framePresenter = new DefaultFramePresenter();
		titleBarPresenter = new DefaultTitleBarPresenter();
		sideBarPresenter = new DefaultSideBarPresenter();
		homeContentPresenter = new HomeContentPresenter();
		displayService = framePresenter;

		// Register services
		BundleContext ctx = compCtx.getBundleContext();
		ctx.registerService(BackgroundService.class, framePresenter, null);
		ctx.registerService(PanelDisplayService.class, framePresenter, null);
		ctx.registerService(TileManagingService.class, homeContentPresenter, null);

		LOGGER.info("Initializing JavaFX");
		new JFXPanel(); // init JavaFX
		Platform.runLater(() -> start(new Stage()));
	}

	private void start(Stage primaryStage) {
		// 防止StyleManager智障读不到CSS
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		mainStage = primaryStage;
		mainStage.initStyle(StageStyle.UNDECORATED);
		initPresenters();
		initLayout();
		mainScene = new Scene(framePresenter.getView().rootPane);
		mainScene.getStylesheets().addAll(LOCATIONS_OF_DEFAULT_CSS);
		mainStage.setScene(mainScene);
		mainStage.show();
	}

	private void initPresenters() {
		// Setup presenters
		titleBarPresenter.setCloseButtonListener(event -> eventAdmin.postEvent(new ApplicationExitEvent()));
		titleBarPresenter.setParentStage(mainStage);

		// TODO window drag

		try {
			framePresenter.initialize();
			titleBarPresenter.initialize();
			sideBarPresenter.initialize();
			homeContentPresenter.initialize();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void initLayout() {
		framePresenter.setTitleBar(titleBarPresenter.getView().rootContainer);
		framePresenter.setSidebar(sideBarPresenter.getView().rootContainer);
		framePresenter.setContent(homeContentPresenter.getView().rootContainer);
	}
}
