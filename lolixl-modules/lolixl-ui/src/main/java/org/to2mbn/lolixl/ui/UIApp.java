package org.to2mbn.lolixl.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.to2mbn.lolixl.ui.container.view.DefaultFrameView;
import org.to2mbn.lolixl.ui.container.view.DefaultTitleBarView;
import org.to2mbn.lolixl.ui.container.view.DefaultUserProfileView;
import org.to2mbn.lolixl.utils.LazyReference;

import java.io.IOException;

public class UIApp extends Application {
	public static final LazyReference<Stage> mainStage = new LazyReference<>();
	public static final LazyReference<Scene> mainScene = new LazyReference<>();

	public static final LazyReference<Region> defaultFrame = new LazyReference<>();
	public static final LazyReference<DefaultFrameView> defaultFrameView = new LazyReference<>();
	public static final LazyReference<Region> defaultTitleBar = new LazyReference<>();
	public static final LazyReference<DefaultTitleBarView> defaultTitleBarView = new LazyReference<>();
	public static final LazyReference<Region> defaultUserProfile = new LazyReference<>();
	public static final LazyReference<DefaultUserProfileView> defaultUserProfileView = new LazyReference<>();

	@Override
	public void start(Stage primaryStage) throws Exception {
		mainStage.set(primaryStage);
		resolveViews();
		mainStage.get().setScene(mainScene.get());
		mainScene.get().getStylesheets().addAll("ui/css/metro.css", "ui/css/components.css");
		mainStage.get().initStyle(StageStyle.UNDECORATED);
		initLayout();
		mainStage.get().show();
	}

	private void resolveViews() throws IOException {
		FXMLLoader loader;

		loader = resolveFXML("/ui/fxml/container/default_title_bar.fxml");
		defaultFrame.set(loader.getRoot());
		defaultTitleBarView.set(loader.getController());

		loader = resolveFXML("/ui/fxml/container/default_user_profile.fxml");
		defaultUserProfile.set(loader.getRoot());
		defaultUserProfileView.set(loader.getController());

		loader = resolveFXML("/ui/fxml/container/default_frame.fxml");
		defaultFrame.set(loader.getRoot());
		defaultFrameView.set(loader.getController());
		mainScene.set(new Scene(loader.getRoot()));
	}

	private void initLayout() {
		defaultFrameView.get().setTitleBar(defaultTitleBar.get());
		defaultFrameView.get().setWidget(defaultUserProfile.get());
	}

	private FXMLLoader resolveFXML(String location) throws IOException {
		return new FXMLLoader(getClass().getResource(location));
	}
}
