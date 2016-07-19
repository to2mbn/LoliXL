package org.to2mbn.lolixl.ui.impl;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventAdmin;
import org.to2mbn.lolixl.ui.BackgroundManagingService;
import org.to2mbn.lolixl.ui.PanelDisplayService;
import org.to2mbn.lolixl.ui.TileManagingService;
import org.to2mbn.lolixl.ui.container.presenter.DefaultFramePresenter;
import org.to2mbn.lolixl.ui.container.presenter.DefaultTitleBarPresenter;
import org.to2mbn.lolixl.ui.container.presenter.DefaultUserProfilePresenter;
import org.to2mbn.lolixl.ui.container.presenter.content.HomeContentPresenter;
import org.to2mbn.lolixl.utils.event.ApplicationExitEvent;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.logging.Logger;

@Component
public class UIApp {

	private static final Logger LOGGER = Logger.getLogger(UIApp.class.getCanonicalName());

	private static final String LOCATION_OF_FRAME = "/ui/fxml/container/default_frame.fxml";
	private static final String LOCATION_OF_TITLE_BAR = "/ui/fxml/container/default_title_bar.fxml";
	private static final String LOCATION_OF_USER_PROFILE = "/ui/fxml/container/default_user_profile.fxml";
	private static final String LOCATION_OF_HOME_CONTENT = "/ui/fxml/container/home_content.fxml";
	private static final String[] LOCATIONS_OF_DEFAULT_CSS = { "/ui/css/metro.css", "/ui/css/components.css" };

	private EventAdmin eventAdmin;

	private Stage mainStage;
	private Scene mainScene;

	private DefaultFramePresenter framePresenter;
	private DefaultTitleBarPresenter titleBarPresenter;
	private DefaultUserProfilePresenter userProfilePresenter;
	private HomeContentPresenter homeContentPresenter;
	private PanelDisplayService displayService;

	@Activate
	public void active(ComponentContext compCtx) {
		// Create presenters
		framePresenter = new DefaultFramePresenter();
		titleBarPresenter = new DefaultTitleBarPresenter();
		userProfilePresenter = new DefaultUserProfilePresenter();
		homeContentPresenter = new HomeContentPresenter();
		displayService = framePresenter;

		// Setup presenters
		titleBarPresenter.setCloseButtonListener(event -> eventAdmin.postEvent(new ApplicationExitEvent()));
		titleBarPresenter.setParentStage(mainStage);

		// Register services
		BundleContext ctx = compCtx.getBundleContext();
		ctx.registerService(BackgroundManagingService.class, framePresenter, null);
		ctx.registerService(PanelDisplayService.class, framePresenter, null);
		ctx.registerService(TileManagingService.class, homeContentPresenter, null);

		LOGGER.info("Init JavaFX");
		new JFXPanel(); // init JavaFX
		Platform.runLater(() -> start(new Stage()));
	}

	private void start(Stage primaryStage) {
		mainStage = primaryStage;
		mainStage.initStyle(StageStyle.UNDECORATED);
		initPresenters();
		initLayout();
		mainScene = new Scene(framePresenter.getView().rootPane);
		mainScene.getStylesheets().addAll(LOCATIONS_OF_DEFAULT_CSS);
		mainStage.setScene(mainScene);
		mainStage.show();
		// TODO: displayService.displayContent(homeContentPresenter.getView().rootContainer);
	}

	private void initPresenters() {
		try {
			framePresenter.initialize(getClass().getResource(LOCATION_OF_FRAME));
			titleBarPresenter.initialize(getClass().getResource(LOCATION_OF_TITLE_BAR));
			userProfilePresenter.initialize(getClass().getResource(LOCATION_OF_USER_PROFILE));
			homeContentPresenter.initialize(getClass().getResource(LOCATION_OF_HOME_CONTENT));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void initLayout() {
		framePresenter.getView().setTitleBar(titleBarPresenter.getView().rootContainer);
		framePresenter.getView().setSidebar(userProfilePresenter.getView().rootContainer);
		framePresenter.getView().setContent(homeContentPresenter.getView().rootContainer);
	}
}
