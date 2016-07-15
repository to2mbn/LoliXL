package org.to2mbn.lolixl.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.to2mbn.lolixl.ui.container.presenter.DefaultFramePresenter;
import org.to2mbn.lolixl.ui.container.presenter.DefaultTitleBarPresenter;
import org.to2mbn.lolixl.ui.container.presenter.DefaultUserProfilePresenter;
import org.to2mbn.lolixl.utils.LazyReference;

import java.io.IOException;

public class UIApp extends Application {
	// TODO 这边的逗比设计以后一定改
	public static final LazyReference<Stage> mainStage = new LazyReference<>();
	public static final LazyReference<Scene> mainScene = new LazyReference<>();
	public static final LazyReference<DefaultFramePresenter> framePresenter = new LazyReference<>();
	public static final LazyReference<DefaultTitleBarPresenter> titleBarPresenter = new LazyReference<>();
	public static final LazyReference<DefaultUserProfilePresenter> userProfilePresenter = new LazyReference<>();

	private static final String LOCATION_OF_FRAME = "/ui/fxml/container/default_frame.fxml";
	private static final String LOCATION_OF_TITLE_BAR = "/ui/fxml/container/default_title_bar.fxml";
	private static final String LOCATION_OF_USER_PROFILE = "/ui/fxml/container/default_user_profile.fxml";

	@Override
	public void start(Stage primaryStage) throws Exception {
		mainStage.set(primaryStage);
		mainStage.get().initStyle(StageStyle.UNDECORATED);

		resolvePresenters();
		mainScene.set(new Scene(framePresenter.get().root.get()));
		mainScene.get().getStylesheets().addAll("ui/css/metro.css", "ui/css/components.css");

		initLayout();
		mainStage.get().setScene(mainScene.get());
		mainStage.get().show();
	}

	private void resolvePresenters() throws IOException {
		framePresenter.get().initialize(getClass().getResource(LOCATION_OF_FRAME));
		titleBarPresenter.get().initialize(getClass().getResource(LOCATION_OF_TITLE_BAR));
		userProfilePresenter.get().initialize(getClass().getResource(LOCATION_OF_USER_PROFILE));
	}

	private void initLayout() {
		framePresenter.get().view.get().setTitleBar(titleBarPresenter.get().root.get());
		framePresenter.get().view.get().setWidget(userProfilePresenter.get().root.get());
	}

	@Override
	public void stop() throws Exception {
		// TODO
	}
}
