package org.to2mbn.lolixl.ui.impl;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.to2mbn.lolixl.i18n.LocaleChangedEvent;
import org.to2mbn.lolixl.ui.BackgroundService;
import org.to2mbn.lolixl.ui.PanelDisplayService;
import org.to2mbn.lolixl.ui.SettingsCategoriesManagingService;
import org.to2mbn.lolixl.ui.SideBarPanelDisplayService;
import org.to2mbn.lolixl.ui.impl.container.presenter.DefaultFramePresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.DefaultSideBarPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.DefaultTitleBarPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.HomeContentPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panel.GameVersionsPanelContentPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panel.HiddenTilesPanelContentPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panel.SettingsPanelContentPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panel.TileManagingPanelContentPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panel.settings.ThemesSettingsPanelPresenter;
import org.to2mbn.lolixl.ui.impl.theme.ThemeConfiguration;
import org.to2mbn.lolixl.ui.theme.Theme;
import org.to2mbn.lolixl.ui.theme.ThemeService;
import org.to2mbn.lolixl.utils.ObservableContext;
import org.to2mbn.lolixl.utils.event.ApplicationExitEvent;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

@Component(immediate = true)
public class UIApp {
	private static final Logger LOGGER = Logger.getLogger(UIApp.class.getCanonicalName());

	private static final String DEFAULT_METRO_STYLE_SHEET = "/ui/css/metro.css";

	@Reference
	private EventAdmin eventAdmin;

	@Reference
	private ThemeService themeLoadingService;

	private final Set<Theme> installedThemes = new HashSet<>();

	private Stage mainStage;
	private Scene mainScene;

	private ObservableContext observableContext;
	private ThemeConfiguration memento;

	private DefaultFramePresenter framePresenter;
	private DefaultTitleBarPresenter titleBarPresenter;
	private DefaultSideBarPresenter sideBarPresenter;
	private HomeContentPresenter homeContentPresenter;
	private TileManagingPanelContentPresenter tileManagingPanelContentPresenter;
	private HiddenTilesPanelContentPresenter hiddenTilesPanelContentPresenter;
	private SettingsPanelContentPresenter settingsPanelContentPresenter;
	private GameVersionsPanelContentPresenter gameVersionsPanelContentPresenter;
	private ThemesSettingsPanelPresenter themesSettingsPanelPresenter;

	@Activate
	public void active(ComponentContext compCtx) {
		LOGGER.info("Initializing UI");
		memento = new ThemeConfiguration();

		// Create presenters
		framePresenter = new DefaultFramePresenter();
		titleBarPresenter = new DefaultTitleBarPresenter();
		sideBarPresenter = new DefaultSideBarPresenter();
		tileManagingPanelContentPresenter = new TileManagingPanelContentPresenter();
		hiddenTilesPanelContentPresenter = new HiddenTilesPanelContentPresenter();
		settingsPanelContentPresenter = new SettingsPanelContentPresenter();
		gameVersionsPanelContentPresenter = new GameVersionsPanelContentPresenter();
		themesSettingsPanelPresenter = new ThemesSettingsPanelPresenter();
		homeContentPresenter = new HomeContentPresenter();

		// Register services
		BundleContext ctx = compCtx.getBundleContext();
		ctx.registerService(BackgroundService.class, framePresenter, null);
		ctx.registerService(PanelDisplayService.class, framePresenter, null);
		ctx.registerService(SideBarPanelDisplayService.class, sideBarPresenter, null);
		ctx.registerService(SettingsCategoriesManagingService.class, settingsPanelContentPresenter, null);
		ctx.registerService(ThemesSettingsPanelPresenter.class, themesSettingsPanelPresenter, null);
		// Register i18n listeners
		Dictionary<String, String> property = new Hashtable<>(Collections.singletonMap(EventConstants.EVENT_TOPIC, LocaleChangedEvent.TOPIC_LOCALE_CHANGED));
		ctx.registerService(EventHandler.class, themesSettingsPanelPresenter, property);

		LOGGER.info("Initializing JavaFX");
		new JFXPanel(); // init JavaFX
		Platform.runLater(() -> start(new Stage()));
	}

	@Deactivate
	public void deactive(ComponentContext compCtx) {
	}

	public List<Theme> getAllThemes() {
		BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
		ServiceReference[] references;
		try {
			references = bundleContext.getAllServiceReferences(Theme.class.getName(), null);
		} catch (InvalidSyntaxException e) {
			throw new Error(e); // impossible
		}
		List<Theme> themes = new LinkedList<>();
		for (ServiceReference reference : references) {
			themes.add((Theme) bundleContext.getService(reference));
		}
		return themes;
	}

	public Stage getMainStage() {
		return mainStage;
	}

	public Scene getMainScene() {
		return mainScene;
	}

	public boolean isThemeInstalled(Theme theme) {
		synchronized (installedThemes) {
			return installedThemes.contains(Objects.requireNonNull(theme));
		}
	}

	private void start(Stage primaryStage) {
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
		mainStage.show();
	}

	private void initPresenters() {
		try {
			framePresenter.initializeView();
			titleBarPresenter.initializeView();
			sideBarPresenter.initializeView();
			tileManagingPanelContentPresenter.initializeView();
			hiddenTilesPanelContentPresenter.initializeView();
			gameVersionsPanelContentPresenter.initializeView();
			themesSettingsPanelPresenter.initializeView();

			// 此人需要压轴出场
			homeContentPresenter.initializeView();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void poseInitPresenters() {
		framePresenter.setStage(mainStage);
		titleBarPresenter.setCloseButtonListener(event -> eventAdmin.postEvent(new ApplicationExitEvent()));
		titleBarPresenter.setParentStage(mainStage);
		themesSettingsPanelPresenter.setUiApp(this);
		homeContentPresenter.setSettingsPanelContentPresenter(settingsPanelContentPresenter);
		homeContentPresenter.setTileManagingPanelContentPresenter(tileManagingPanelContentPresenter);

		framePresenter.postInitialize();
		titleBarPresenter.postInitialize();
		sideBarPresenter.postInitialize();
		tileManagingPanelContentPresenter.postInitialize();
		hiddenTilesPanelContentPresenter.postInitialize();
		gameVersionsPanelContentPresenter.postInitialize();
		themesSettingsPanelPresenter.postInitialize();

		// 此人需要压轴出场
		homeContentPresenter.postInitialize();
	}

	private void initLayout() {
		framePresenter.setTitleBar(titleBarPresenter.getView().rootContainer);
		framePresenter.setSidebar(sideBarPresenter.getView().rootContainer);
		framePresenter.setContent(homeContentPresenter.getView().rootContainer);
	}
}
