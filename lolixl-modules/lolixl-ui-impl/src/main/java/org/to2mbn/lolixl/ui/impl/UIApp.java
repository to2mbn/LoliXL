package org.to2mbn.lolixl.ui.impl;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventAdmin;
import org.to2mbn.lolixl.ui.BackgroundService;
import org.to2mbn.lolixl.ui.PanelDisplayService;
import org.to2mbn.lolixl.ui.SettingsCategoriesManagingService;
import org.to2mbn.lolixl.ui.SideBarPanelDisplayService;
import org.to2mbn.lolixl.ui.SideBarTileService;
import org.to2mbn.lolixl.ui.impl.container.presenter.DefaultFramePresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.DefaultSideBarPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.DefaultTitleBarPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.content.HomeContentPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panelcontent.GameVersionsPanelContentPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panelcontent.HiddenTilesPanelContentPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panelcontent.SettingsPanelContentPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panelcontent.TileManagingPanelContentPresenter;
import org.to2mbn.lolixl.ui.impl.theme.DefaultTheme;
import org.to2mbn.lolixl.ui.impl.theme.management.InstalledThemesConfiguration;
import org.to2mbn.lolixl.ui.theme.BundledTheme;
import org.to2mbn.lolixl.ui.theme.Theme;
import org.to2mbn.lolixl.ui.theme.exception.InvalidBundledThemeException;
import org.to2mbn.lolixl.ui.theme.management.ThemeManagementService;
import org.to2mbn.lolixl.utils.FXUtils;
import org.to2mbn.lolixl.utils.ObservableContext;
import org.to2mbn.lolixl.utils.event.ApplicationExitEvent;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.logging.Logger;

@Component
@Service({ ThemeManagementService.class })
public class UIApp implements ThemeManagementService, ConfigurationCategory<InstalledThemesConfiguration> {
	private static final Logger LOGGER = Logger.getLogger(UIApp.class.getCanonicalName());

	private static final String DEFAULT_METRO_STYLE_SHEET = "/ui/css/metro.css";

	@Reference
	private EventAdmin eventAdmin;

	private Stage mainStage;
	private Scene mainScene;

	private ObservableContext observableContext;
	private InstalledThemesConfiguration configuration;
	private Theme installedTheme;

	private DefaultFramePresenter framePresenter;
	private DefaultTitleBarPresenter titleBarPresenter;
	private DefaultSideBarPresenter sideBarPresenter;
	private HomeContentPresenter homeContentPresenter;
	private TileManagingPanelContentPresenter tileManagingPanelContentPresenter;
	private HiddenTilesPanelContentPresenter hiddenTilesPanelContentPresenter;
	private SettingsPanelContentPresenter settingsPanelContentPresenter;
	private GameVersionsPanelContentPresenter gameVersionsPanelContentPresenter;

	@Activate
	public void active(ComponentContext compCtx) {
		LOGGER.info("Initializing UI");
		configuration = new InstalledThemesConfiguration();

		// Create presenters
		framePresenter = new DefaultFramePresenter();
		titleBarPresenter = new DefaultTitleBarPresenter();
		sideBarPresenter = new DefaultSideBarPresenter();
		homeContentPresenter = new HomeContentPresenter();
		tileManagingPanelContentPresenter = new TileManagingPanelContentPresenter();
		hiddenTilesPanelContentPresenter = new HiddenTilesPanelContentPresenter();
		settingsPanelContentPresenter = new SettingsPanelContentPresenter();
		gameVersionsPanelContentPresenter = new GameVersionsPanelContentPresenter();

		// Register services
		BundleContext ctx = compCtx.getBundleContext();
		ctx.registerService(BackgroundService.class, framePresenter, null);
		ctx.registerService(PanelDisplayService.class, framePresenter, null);
		ctx.registerService(SideBarTileService.class, homeContentPresenter, null);
		ctx.registerService(SideBarPanelDisplayService.class, sideBarPresenter, null);
		ctx.registerService(SettingsCategoriesManagingService.class, settingsPanelContentPresenter, null);

		LOGGER.info("Initializing JavaFX");
		new JFXPanel(); // init JavaFX
		Platform.runLater(() -> start(new Stage()));
	}

	@Override
	public void installTheme(Theme theme) throws InvalidBundledThemeException {
		FXUtils.checkFxThread();
		if (installedTheme != null) {
			uninstallTheme(installedTheme);
		}
		if (theme instanceof BundledTheme) {
			String bundleLocation = (String) theme.getMeta().get(BundledTheme.INTERNAL_META_KEY_BUNDLE_URL);
			if (bundleLocation == null || bundleLocation.isEmpty()) {
				throw new InvalidBundledThemeException("location url of the theme can not be null");
			}
			configuration.urls.add(bundleLocation);
			ClassLoader resourceLoader = ((BundledTheme) theme).getResourceLoader();
			Thread.currentThread().setContextClassLoader(resourceLoader);
		}
		mainScene.getStylesheets().retainAll(DEFAULT_METRO_STYLE_SHEET);
		mainScene.getStylesheets().addAll(theme.getStyleSheets());
		installedTheme = theme;
	}

	@Override
	public void uninstallTheme(Theme theme) {
		FXUtils.checkFxThread();
		mainScene.getStylesheets().removeAll(theme.getStyleSheets());
		installedTheme = null;
	}

	@Override
	public Theme getInstalledTheme() {
		return installedTheme;
	}

	@Override
	public void setObservableContext(ObservableContext ctx) {
		observableContext = ctx;
	}

	@Override
	public InstalledThemesConfiguration store() {
		return configuration;
	}

	@Override
	public void restore(InstalledThemesConfiguration memento) {
		configuration.urls = memento.urls;
	}

	@Override
	public Class<? extends InstalledThemesConfiguration> getMementoType() {
		return configuration.getClass();
	}

	@Override
	public String getLocalizedName() {
		return null;
	}

	@Override
	public Region createConfiguringPanel() {
		return null;
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

		try {
			installTheme(new DefaultTheme());
		} catch (InvalidBundledThemeException e) {
			throw new Error(e); // impossible
		}
		mainStage.show();
	}

	private void initPresenters() {
		try {
			framePresenter.initializeView();
			titleBarPresenter.initializeView();
			sideBarPresenter.initializeView();
			homeContentPresenter.initializeView();
			tileManagingPanelContentPresenter.initializeView();
			hiddenTilesPanelContentPresenter.initializeView();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		titleBarPresenter.setCloseButtonListener(event -> eventAdmin.postEvent(new ApplicationExitEvent()));
		titleBarPresenter.setParentStage(mainStage);
		homeContentPresenter.setTileManagingPanelContentPresenter(tileManagingPanelContentPresenter);
		homeContentPresenter.setDefaultFramePresenter(framePresenter);
		homeContentPresenter.setHiddenTilesPanelContentPresenter(hiddenTilesPanelContentPresenter);
		tileManagingPanelContentPresenter.setHomeContentPresenter(homeContentPresenter);

		framePresenter.postInitialize();
		titleBarPresenter.postInitialize();
		sideBarPresenter.postInitialize();
		homeContentPresenter.postInitialize();
		tileManagingPanelContentPresenter.postInitialize();
		hiddenTilesPanelContentPresenter.postInitialize();
	}

	private void initLayout() {
		framePresenter.setTitleBar(titleBarPresenter.getView().rootContainer);
		framePresenter.setSidebar(sideBarPresenter.getView().rootContainer);
		framePresenter.setContent(homeContentPresenter.getView().rootContainer);
	}
}
