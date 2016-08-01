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
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.to2mbn.lolixl.ui.BackgroundService;
import org.to2mbn.lolixl.ui.PanelDisplayService;
import org.to2mbn.lolixl.ui.SettingsCategoriesManagingService;
import org.to2mbn.lolixl.ui.SideBarPanelDisplayService;
import org.to2mbn.lolixl.ui.impl.container.presenter.DefaultFramePresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.DefaultSideBarPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.DefaultTitleBarPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.HomeContentPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panel.downloads.DownloadCenterPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panel.settings.SettingsPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panel.settings.ThemesPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panel.sidebar.GameVersionsPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panel.tiles.HiddenTilesPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panel.tiles.TileManagingPresenter;
import org.to2mbn.lolixl.ui.impl.event.UIPostInitializationEvent;
import org.to2mbn.lolixl.utils.event.ApplicationExitEvent;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.logging.Logger;

@Component(immediate = true)
public class UIApp {
	private static final Logger LOGGER = Logger.getLogger(UIApp.class.getCanonicalName());
	private static final String DEFAULT_METRO_STYLE_SHEET = "/ui/css/metro.css";

	@Reference
	private EventAdmin eventAdmin;

	private Stage mainStage;
	private Scene mainScene;

	private DefaultFramePresenter framePresenter;
	private DefaultTitleBarPresenter titleBarPresenter;
	private DefaultSideBarPresenter sideBarPresenter;
	private HomeContentPresenter homeContentPresenter;
	private TileManagingPresenter tileManagingPresenter;
	private HiddenTilesPresenter hiddenTilesPresenter;
	private SettingsPresenter settingsPresenter;
	private GameVersionsPresenter gameVersionsPresenter;
	private ThemesPresenter themesPresenter;
	private DownloadCenterPresenter downloadCenterPresenter;

	@Activate
	public void active(ComponentContext compCtx) {
		LOGGER.info("Initializing UI");

		LOGGER.info("Creating presenter instances");
		// Create presenters
		framePresenter = new DefaultFramePresenter();
		titleBarPresenter = new DefaultTitleBarPresenter();
		sideBarPresenter = new DefaultSideBarPresenter();
		tileManagingPresenter = new TileManagingPresenter();
		hiddenTilesPresenter = new HiddenTilesPresenter();
		settingsPresenter = new SettingsPresenter();
		gameVersionsPresenter = new GameVersionsPresenter();
		themesPresenter = new ThemesPresenter();
		homeContentPresenter = new HomeContentPresenter();
		downloadCenterPresenter = new DownloadCenterPresenter();

		LOGGER.info("Registering services");
		// Register services
		BundleContext ctx = compCtx.getBundleContext();
		ctx.registerService(BackgroundService.class, framePresenter, null);
		ctx.registerService(PanelDisplayService.class, framePresenter, null);
		ctx.registerService(SideBarPanelDisplayService.class, sideBarPresenter, null);
		ctx.registerService(SettingsCategoriesManagingService.class, settingsPresenter, null);
		ctx.registerService(ThemesPresenter.class, themesPresenter, null);

		LOGGER.info("Initializing JavaFX engine");
		new JFXPanel(); // init JavaFX engine
		Platform.runLater(() -> start(new Stage()));
	}

	public Stage getMainStage() {
		return mainStage;
	}

	public Scene getMainScene() {
		return mainScene;
	}


	private void start(Stage primaryStage) {
		LOGGER.info("Initializing window");
		// 防止StyleManager智障读不到CSS
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		mainStage = primaryStage;
		mainStage.initStyle(StageStyle.UNDECORATED);
		initPresenters();
		initLayout();
		mainScene = new Scene(framePresenter.getView().rootContainer);
		mainScene.getStylesheets().add(DEFAULT_METRO_STYLE_SHEET);
		mainStage.setScene(mainScene);
		poseInitPresenters();
		fireEvent(new UIPostInitializationEvent(this));
		mainStage.show();
	}

	private void initPresenters() {
		LOGGER.info("Initializing presenters' views");
		try {
			framePresenter.initializeView();
			titleBarPresenter.initializeView();
			sideBarPresenter.initializeView();
			tileManagingPresenter.initializeView();
			hiddenTilesPresenter.initializeView();
			settingsPresenter.initializeView();
			gameVersionsPresenter.initializeView();
			themesPresenter.initializeView();
			downloadCenterPresenter.initializeView();

			// 此人需要压轴出场
			homeContentPresenter.initializeView();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void poseInitPresenters() {
		LOGGER.info("Post-initializing presenters");
		framePresenter.setStage(mainStage);
		titleBarPresenter.setCloseButtonListener(event -> eventAdmin.postEvent(new ApplicationExitEvent()));
		titleBarPresenter.setParentStage(mainStage);
		homeContentPresenter.setDownloadCenterPresenter(downloadCenterPresenter);
		homeContentPresenter.setSettingsPresenter(settingsPresenter);
		homeContentPresenter.setTileManagingPresenter(tileManagingPresenter);

		framePresenter.postInitialize();
		titleBarPresenter.postInitialize();
		sideBarPresenter.postInitialize();
		tileManagingPresenter.postInitialize();
		hiddenTilesPresenter.postInitialize();
		settingsPresenter.postInitialize();
		gameVersionsPresenter.postInitialize();
		themesPresenter.postInitialize();
		downloadCenterPresenter.postInitialize();

		// 此人需要压轴出场
		homeContentPresenter.postInitialize();
	}

	private void initLayout() {
		LOGGER.info("Initializing layout");
		framePresenter.setTitleBar(titleBarPresenter.getView().rootContainer);
		framePresenter.setSidebar(sideBarPresenter.getView().rootContainer);
		framePresenter.setContent(homeContentPresenter.getView().rootContainer);
	}

	private void fireEvent(Event event) {
		eventAdmin.postEvent(event);
	}
}
