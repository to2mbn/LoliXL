package org.to2mbn.lolixl.ui.container.presenter;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.lolixl.ui.container.presenter.content.HomeContentPresenter;
import org.to2mbn.lolixl.ui.container.view.DefaultFrameView;
import org.to2mbn.lolixl.ui.service.BackgroundManagingService;
import org.to2mbn.lolixl.ui.service.DisplayPanesListener;
import org.to2mbn.lolixl.ui.service.DisplayService;

import java.io.IOException;
import java.net.URL;

@Component
@Service({DefaultFramePresenter.class, BackgroundManagingService.class})
public class DefaultFramePresenter extends Presenter<DefaultFrameView> implements BackgroundManagingService, DisplayPanesListener {
	@Reference
	private DisplayService displayService;

	@Reference
	private HomeContentPresenter homeContentPresenter;

	@Override
	public void initialize(URL fxmlLocation) throws IOException {
		super.initialize(fxmlLocation);
		displayService.addListener(this);
	}

	@Override
	public void changeBackground(Background background) {
		Platform.runLater(() -> view.rootPane.setBackground(background));
	}

	@Override
	public Background getCurrentBackground() {
		return view.rootPane.getBackground();
	}

	@Override
	public void onPaneAdded(Pane pane) {
		Platform.runLater(() -> {
			BorderPane content = view.contentPane;
			pane.resize(content.getWidth(), content.getHeight());
			ParallelTransition animation = generateAnimation(pane, false);
			animation.setOnFinished(event -> view.setContent(pane));
			animation.play();
		});
	}

	@Override
	public void onPaneRemoved(Pane paneRemoved, Pane previousPane) {
		Platform.runLater(() -> {
			ParallelTransition animation = generateAnimation(paneRemoved, true);
			animation.setOnFinished(event -> {
				if (previousPane == null) {
					view.setContent(homeContentPresenter.getView().rootContainer);
				} else {
					view.setContent(previousPane);
				}
			});
			animation.play();
		});
	}

	private ParallelTransition generateAnimation(Pane pane, boolean reverse) {
		BorderPane content = view.contentPane;
		TranslateTransition tran = new TranslateTransition(Duration.seconds(1), pane);
		tran.setCycleCount(TranslateTransition.INDEFINITE);
		tran.setFromX(content.getLayoutX() + content.getWidth() / 5);
		tran.setToX(content.getLayoutX());

		FadeTransition fade = new FadeTransition(Duration.seconds(1), pane);
		fade.setCycleCount(FadeTransition.INDEFINITE);
		fade.setFromValue(0.5);
		fade.setToValue(1.0);

		ParallelTransition parallel = new ParallelTransition();
		parallel.getChildren().addAll(tran, fade);
		if (reverse) {
			parallel.setAutoReverse(true);
			parallel.jumpTo(Duration.seconds(1));
		}
		return parallel;
	}
}
