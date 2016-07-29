package org.to2mbn.lolixl.ui.impl;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
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
import org.to2mbn.lolixl.core.config.ConfigurationCategory;
import org.to2mbn.lolixl.ui.*;
import org.to2mbn.lolixl.ui.impl.container.presenter.DefaultFramePresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.DefaultSideBarPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.DefaultTitleBarPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.content.HomeContentPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panelcontent.*;
import org.to2mbn.lolixl.ui.impl.theme.DefaultTheme;
import org.to2mbn.lolixl.ui.impl.theme.management.InstalledThemeMemento;
import org.to2mbn.lolixl.ui.theme.Theme;
import org.to2mbn.lolixl.ui.theme.exception.InvalidThemeException;
import org.to2mbn.lolixl.ui.theme.loading.ThemeLoadingService;
import org.to2mbn.lolixl.utils.ObservableContext;
import org.to2mbn.lolixl.utils.event.ApplicationExitEvent;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(immediate = true)
public class UIApp implements ConfigurationCategory<InstalledThemeMemento> {
	private static final Logger LOGGER = Logger.getLogger(UIApp.class.getCanonicalName());

	private static final String DEFAULT_METRO_STYLE_SHEET = "/ui/css/metro.css";

	@Reference
	private EventAdmin eventAdmin;

	@Reference
	private ThemeLoadingService themeLoadingService;

	private Stage mainStage;
	private Scene mainScene;

	private ObservableContext observableContext;
	private InstalledThemeMemento memento;

	private DefaultFramePresenter framePresenter;
	private DefaultTitleBarPresenter titleBarPresenter;
	private DefaultSideBarPresenter sideBarPresenter;
	private HomeContentPresenter homeContentPresenter;
	private TileManagingPanelContentPresenter tileManagingPanelContentPresenter;
	private HiddenTilesPanelContentPresenter hiddenTilesPanelContentPresenter;
	private SettingsPanelContentPresenter settingsPanelContentPresenter;
	private GameVersionsPanelContentPresenter gameVersionsPanelContentPresenter;
	private ThemesContentPanelPresenter themesContentPanelPresenter;

	@Activate
	public void active(ComponentContext compCtx) {
		LOGGER.info("Initializing UI");
		memento = new InstalledThemeMemento();

		// Create presenters
		framePresenter = new DefaultFramePresenter();
		titleBarPresenter = new DefaultTitleBarPresenter();
		sideBarPresenter = new DefaultSideBarPresenter();
		homeContentPresenter = new HomeContentPresenter();
		tileManagingPanelContentPresenter = new TileManagingPanelContentPresenter();
		hiddenTilesPanelContentPresenter = new HiddenTilesPanelContentPresenter();
		settingsPanelContentPresenter = new SettingsPanelContentPresenter();
		gameVersionsPanelContentPresenter = new GameVersionsPanelContentPresenter();
		themesContentPanelPresenter = new ThemesContentPanelPresenter();

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

	@Deactivate
	public void deactive(ComponentContext compCtx) {
		memento.lastLoadedThemePaths.clear();
		for (Theme theme : getAllThemes()) {
			if (theme.getMeta().containsKey(Theme.INTERNAL_PROPERTY_KEY_PACKAGE_PATH)) {
				memento.lastLoadedThemePaths.add((String) theme.getMeta().get(Theme.INTERNAL_PROPERTY_KEY_PACKAGE_PATH));
			}
		}
		observableContext.notifyChanged();
		LOGGER.info("Tracked loaded theme packages");
	}

	@Override
	public void setObservableContext(ObservableContext ctx) {
		observableContext = ctx;
	}

	@Override
	public InstalledThemeMemento store() {
		return memento;
	}

	@Override
	public void restore(InstalledThemeMemento _memento) {
		memento.lastLoadedThemePaths = _memento.lastLoadedThemePaths;
		memento.lastInstalledThemeIds = _memento.lastInstalledThemeIds;
	}

	@Override
	public Class<? extends InstalledThemeMemento> getMementoType() {
		return memento.getClass();
	}

	@Override
	public String getLocalizedName() {
		return null;
	}

	@Override
	public Region createConfiguringPanel() {
		return null;
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

	public void installTheme(Theme theme) throws InvalidThemeException {
		String themeId = theme.getId();
		if (themeId == null || themeId.isEmpty()) {
			throw new InvalidThemeException("ID meta of a theme can not be null");
		}

		String themePackagePath = (String) theme.getMeta().get(Theme.INTERNAL_PROPERTY_KEY_PACKAGE_PATH);
		if (themePackagePath != null && !themePackagePath.isEmpty()) {
			memento.lastLoadedThemePaths.add(themePackagePath);
		}
		memento.lastInstalledThemeIds.add(themeId);
		observableContext.notifyChanged();

		ClassLoader resourceLoader = theme.getResourceLoader();
		Thread.currentThread().setContextClassLoader(resourceLoader);
		mainScene.getStylesheets().retainAll(DEFAULT_METRO_STYLE_SHEET);
		mainScene.getStylesheets().addAll(theme.getStyleSheets());
		LOGGER.info("Installed theme '" + themeId + "'");
	}

	public void uninstallTheme(Theme theme) {
		mainScene.getStylesheets().removeAll(theme.getStyleSheets());
	}

	public Stage getMainStage() {
		return mainStage;
	}

	public Scene getMainScene() {
		return mainScene;
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
		poseInitPresenters();
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
	}

	private void poseInitPresenters() {
		framePresenter.setStage(mainStage);
		titleBarPresenter.setCloseButtonListener(event -> eventAdmin.postEvent(new ApplicationExitEvent()));
		titleBarPresenter.setParentStage(mainStage);
		homeContentPresenter.setTileManagingPanelContentPresenter(tileManagingPanelContentPresenter);
		homeContentPresenter.setDefaultFramePresenter(framePresenter);
		homeContentPresenter.setHiddenTilesPanelContentPresenter(hiddenTilesPanelContentPresenter);
		tileManagingPanelContentPresenter.setHomeContentPresenter(homeContentPresenter);
		themesContentPanelPresenter.setUiApp(this);

		framePresenter.postInitialize();
		titleBarPresenter.postInitialize();
		sideBarPresenter.postInitialize();
		homeContentPresenter.postInitialize();
		tileManagingPanelContentPresenter.postInitialize();
		hiddenTilesPanelContentPresenter.postInitialize();
		themesContentPanelPresenter.postInitialize();
	}

	private void initLayout() {
		framePresenter.setTitleBar(titleBarPresenter.getView().rootContainer);
		framePresenter.setSidebar(sideBarPresenter.getView().rootContainer);
		framePresenter.setContent(homeContentPresenter.getView().rootContainer);
	}

	private void initTheme() {
		for (String path : memento.lastLoadedThemePaths) {
			if (!Files.exists(Paths.get(path))) {
				LOGGER.info("Last loaded theme path '" + path + "' is invalid, deleting it");
				memento.lastLoadedThemePaths.remove(path);
				continue;
			}
			try {
				themeLoadingService.loadAndPublish(Paths.get(path).toUri().toURL());
			} catch (IOException | InvalidThemeException e) {
				LOGGER.log(Level.WARNING, "Failed to load theme package from '" + path + "'", e);
			}
		}

		List<String> lastIds = memento.lastInstalledThemeIds;
		if (lastIds != null && !lastIds.isEmpty()) {
			for (String id : lastIds) {
				try {
					installTheme(findThemeById(id));
				} catch (InvalidSyntaxException | InvalidThemeException e) {
					LOGGER.log(Level.WARNING, "Failed to load last theme package '" + id + "'", e);
				}
			}

		} else {
			try {
				installTheme(new DefaultTheme());
			} catch (InvalidThemeException e) {
				throw new Error(e); // impossible
			}
		}
	}

	private Theme findThemeById(String id) throws InvalidSyntaxException {
		Theme theme = null;
		BundleContext ctx = FrameworkUtil.getBundle(getClass()).getBundleContext();
		Collection<ServiceReference<Theme>> references = ctx.getServiceReferences(Theme.class, "(" + Theme.PROPERTY_KEY_ID + "=" + id + ")");
		if (references.size() > 0) {
			theme = ctx.getService(references.iterator().next());
		}
		return theme;
	}
}
