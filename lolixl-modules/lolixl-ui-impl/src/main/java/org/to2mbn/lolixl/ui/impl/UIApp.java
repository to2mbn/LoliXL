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
import org.to2mbn.lolixl.core.config.ConfigurationCategory;
import org.to2mbn.lolixl.ui.*;
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
import org.to2mbn.lolixl.ui.theme.exception.InvalidThemeException;
import org.to2mbn.lolixl.ui.theme.loading.ThemeLoadingService;
import org.to2mbn.lolixl.ui.theme.management.ThemeManagementService;
import org.to2mbn.lolixl.utils.FXUtils;
import org.to2mbn.lolixl.utils.ObservableContext;
import org.to2mbn.lolixl.utils.event.ApplicationExitEvent;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(immediate = true)
@Service({ ThemeManagementService.class })
public class UIApp implements ThemeManagementService, ConfigurationCategory<InstalledThemesConfiguration> {
	private static final Logger LOGGER = Logger.getLogger(UIApp.class.getCanonicalName());

	private static final String DEFAULT_METRO_STYLE_SHEET = "/ui/css/metro.css";

	@Reference
	private EventAdmin eventAdmin;

	@Reference
	private ThemeLoadingService themeLoadingService;

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
	public void installTheme(Theme theme) throws InvalidThemeException {
		FXUtils.checkFxThread();
		if (installedTheme != null) {
			uninstallTheme(installedTheme);
		}
		String themeId = (String) theme.getMeta().get(Theme.META_KEY_ID);
		if (themeId == null || themeId.isEmpty()) {
			throw new InvalidThemeException("ID meta of a theme can not be null");
		}
		if (theme instanceof BundledTheme) {
			String bundleLocation = (String) theme.getMeta().get(BundledTheme.INTERNAL_META_KEY_BUNDLE_URL);
			if (bundleLocation == null || bundleLocation.isEmpty()) {
				throw new InvalidThemeException("location url of the theme can not be null");
			}
			configuration.urls.add(bundleLocation);
			configuration.lastInstalledBundledThemeId = themeId;
			observableContext.notifyChanged();
			ClassLoader resourceLoader = ((BundledTheme) theme).getResourceLoader();
			Thread.currentThread().setContextClassLoader(resourceLoader);
		} else {
			configuration.lastInstalledBundledThemeId = "";
			observableContext.notifyChanged();
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
		initTheme();
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

	private void initTheme() {
		try {
			installTheme(new DefaultTheme());
		} catch (InvalidThemeException e) {
			throw new Error(e); // impossible
		}

		LOGGER.info("Loading bundled themes from configuration");
		for (String url : configuration.urls) {
			try {
				themeLoadingService.loadFromURL(new URL(url));
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, "Failed to load bundled theme from '" + url + "'", e);
			}
		}

		if (!configuration.lastInstalledBundledThemeId.isEmpty()) {
			Optional<Theme> lastTheme = themeLoadingService.findThemeById(configuration.lastInstalledBundledThemeId);
			if (!lastTheme.isPresent()) {
				LOGGER.warning("Missing last bundled theme: " + configuration.lastInstalledBundledThemeId);
			} else {
				LOGGER.info("Installing last bundled theme: " + configuration.lastInstalledBundledThemeId);
				try {
					installTheme(lastTheme.get());
				} catch (InvalidThemeException e) {
					LOGGER.log(Level.WARNING, "Failed to install last bundled theme", e);
				}
			}
		}
	}
}
