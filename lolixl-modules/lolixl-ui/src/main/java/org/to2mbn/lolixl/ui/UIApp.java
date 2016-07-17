package org.to2mbn.lolixl.ui;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.lolixl.ui.container.presenter.DefaultFramePresenter;
import org.to2mbn.lolixl.ui.container.presenter.DefaultTitleBarPresenter;
import org.to2mbn.lolixl.ui.container.presenter.DefaultUserProfilePresenter;
import org.to2mbn.lolixl.ui.container.presenter.content.HomeContentPresenter;
import org.to2mbn.lolixl.ui.service.ContentDisplayService;
import java.io.IOException;
import java.io.UncheckedIOException;

@Component
@Service({ UIPrimaryReferenceProvider.class })
public class UIApp implements UIPrimaryReferenceProvider {

	private static final String LOCATION_OF_FRAME = "/ui/fxml/container/default_frame.fxml";
	private static final String LOCATION_OF_TITLE_BAR = "/ui/fxml/container/default_title_bar.fxml";
	private static final String LOCATION_OF_USER_PROFILE = "/ui/fxml/container/default_user_profile.fxml";
	private static final String LOCATION_OF_HOME_CONTENT = "/ui/fxml/container/home_content.fxml";
	private static final String[] LOCATIONS_OF_DEFAULT_CSS = { "/ui/css/metro.css", "/ui/css/components.css" };

	private Stage mainStage;
	private Scene mainScene;

	@Reference
	private DefaultFramePresenter framePresenter;

	@Reference
	private DefaultTitleBarPresenter titleBarPresenter;

	@Reference
	private DefaultUserProfilePresenter userProfilePresenter;

	@Reference
	private HomeContentPresenter homeContentPresenter;

	@Reference
	private ContentDisplayService displayService;

	@Activate
	public void active() {
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
		displayService.displayContent(homeContentPresenter.getView().rootContainer);
	}

	private void stop() {
		// TODO
	}

	@Override
	public Stage getMainStage() {
		return mainStage;
	}

	@Override
	public Scene getMainScene() {
		return mainScene;
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
		framePresenter.getView().setWidget(userProfilePresenter.getView().rootContainer);
		framePresenter.getView().setContent(homeContentPresenter.getView().rootContainer);
	}
}
