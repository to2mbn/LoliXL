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
import org.to2mbn.lolixl.ui.impl.container.presenter.panelcontent.GameVersionsPanelContentPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panelcontent.HiddenTilesPanelContentPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panelcontent.SettingsPanelContentPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panelcontent.TileManagingPanelContentPresenter;
import org.to2mbn.lolixl.ui.impl.theme.DefaultTheme;
import org.to2mbn.lolixl.ui.impl.theme.management.InstalledThemeMemento;
import org.to2mbn.lolixl.ui.theme.Theme;
import org.to2mbn.lolixl.ui.theme.exception.InvalidThemeException;
import org.to2mbn.lolixl.ui.theme.loading.ThemeLoadingService;
import org.to2mbn.lolixl.utils.ObservableContext;
import org.to2mbn.lolixl.utils.event.ApplicationExitEvent;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
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
		memento.lastLoadedThemePackageUrls.clear();
		for (Theme theme : getAllThemes()) {
			if (theme.getMeta().containsKey(Theme.INTERNAL_PROPERTY_KEY_PACKAGE_URL)) {
				memento.lastLoadedThemePackageUrls.add((String) theme.getMeta().get(Theme.INTERNAL_PROPERTY_KEY_PACKAGE_URL));
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
		memento.lastLoadedThemePackageUrls = _memento.lastLoadedThemePackageUrls;
		memento.lastInstalledThemeId = _memento.lastInstalledThemeId;
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
		if (installedTheme != null) {
			uninstallTheme(installedTheme);
		}
		String themeId = theme.getId();
		if (themeId == null || themeId.isEmpty()) {
			throw new InvalidThemeException("ID meta of a theme can not be null");
		}

		String bundleLocation = (String) theme.getMeta().get(Theme.INTERNAL_PROPERTY_KEY_PACKAGE_URL);
		if (bundleLocation != null && !bundleLocation.isEmpty()) {
			memento.lastLoadedThemePackageUrls.add(bundleLocation);
		}
		memento.lastInstalledThemeId = themeId;
		observableContext.notifyChanged();

		ClassLoader resourceLoader = theme.getResourceLoader();
		Thread.currentThread().setContextClassLoader(resourceLoader);
		mainScene.getStylesheets().retainAll(DEFAULT_METRO_STYLE_SHEET);
		mainScene.getStylesheets().addAll(theme.getStyleSheets());
		installedTheme = theme;
		LOGGER.info("Installed theme '" + themeId + "'");
	}

	public void uninstallTheme(Theme theme) {
		mainScene.getStylesheets().removeAll(theme.getStyleSheets());
		installedTheme = null;
	}

	public Theme getInstalledTheme() {
		return installedTheme;
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
		for (String strUrl : memento.lastLoadedThemePackageUrls) {
			URL url;
			try {
				url = new URL(strUrl);
			} catch (MalformedURLException e) {
				LOGGER.log(Level.WARNING, "The theme package url '" + strUrl + "' is in a valid format, it will be ignored", e);
				continue;
			}
			try {
				themeLoadingService.loadAndPublish(url);
			} catch (IOException | InvalidThemeException e) {
				LOGGER.log(Level.WARNING, "Failed to load theme package from '" + strUrl + "'", e);
			}
		}

		String lastId = memento.lastInstalledThemeId;
		if (lastId != null && !lastId.isEmpty()) {
			BundleContext ctx = FrameworkUtil.getBundle(getClass()).getBundleContext();
			try {
				Collection<ServiceReference<Theme>> references = ctx.getServiceReferences(Theme.class, "(" + Theme.PROPERTY_KEY_ID + "=" + lastId + ")");
				if (references.size() > 0) {
					installTheme(ctx.getService(references.iterator().next()));
				}
			} catch (InvalidSyntaxException | InvalidThemeException e) {
				LOGGER.log(Level.WARNING, "Failed to load last theme package '" + lastId + "'", e);
			}
		} else {
			try {
				installTheme(new DefaultTheme());
			} catch (InvalidThemeException e) {
				throw new Error(e); // impossible
			}
		}
	}
}
