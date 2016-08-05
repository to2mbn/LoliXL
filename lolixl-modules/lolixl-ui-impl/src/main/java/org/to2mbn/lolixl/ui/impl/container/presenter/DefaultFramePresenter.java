package org.to2mbn.lolixl.ui.impl.container.presenter;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.Observable;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.ui.BackgroundService;
import org.to2mbn.lolixl.ui.Panel;
import org.to2mbn.lolixl.ui.PanelDisplayService;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.MainStage;
import org.to2mbn.lolixl.ui.impl.component.model.PanelImpl;
import org.to2mbn.lolixl.ui.impl.component.view.panel.PanelView;
import org.to2mbn.lolixl.ui.impl.container.view.DefaultFrameView;
import org.to2mbn.lolixl.utils.FXUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service({ BackgroundService.class, PanelDisplayService.class, DefaultFramePresenter.class })
@Component(immediate = true)
public class DefaultFramePresenter extends Presenter<DefaultFrameView> implements BackgroundService, PanelDisplayService {

	private static final String FXML_LOCATION = "/ui/fxml/container/default_frame.fxml";

	@Reference(target = "(" + MainStage.PROPERTY_STAGE_ID + "=" + MainStage.MAIN_STAGE_ID + ")")
	private Stage stage;

	@Reference
	private DefaultSideBarPresenter sideBarPresenter;

	@Reference
	private HomeContentPresenter homeContentPresenter;

	private final Queue<PanelEntry> panels = new ConcurrentLinkedQueue<>();

	@Activate
	public void active(ComponentContext compCtx) {
		super.active();
	}

	@Override
	protected void initializePresenter() {
		view.sidebarPane.getChildren().add(sideBarPresenter.getView().rootContainer);
		view.contentPane.getChildren().add(homeContentPresenter.getView().rootContainer);

		view.rootContainer.sceneProperty().addListener((Observable dummy) -> {
			Scene scene = view.rootContainer.getScene();
			if (scene != null) {
				view.rootContainer.getScene().getAccelerators().put(
						new KeyCodeCombination(KeyCode.ESCAPE),
						() -> getCurrent().ifPresent(Panel::hide));
			}
		});
	}

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
	}

	@Override
	public void setBackground(Background background) {
		Objects.requireNonNull(background);
		FXUtils.checkFxThread();
		view.rootContainer.setBackground(background);
	}

	@Override
	public Background getBackground() {
		return view.rootContainer.getBackground();
	}

	@Override
	public Panel newPanel() {
		return new PanelImpl(this::displayPanelEntry, this::hideCurrent);
	}

	@Override
	public Optional<Panel> getCurrent() {
		PanelEntry entry = panels.peek();
		return entry != null ? Optional.of(entry.model) : Optional.empty();
	}

	@Override
	public Panel[] getOpenedPanels() {
		return panels.stream().map(entry -> entry.model).toArray(Panel[]::new);
	}

	private void displayPanelEntry(Panel model) {
		PanelEntry entry;
		try {
			entry = new PanelEntry(model, new PanelView(model));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		panels.offer(entry);
		// 先隐藏添加的面板 等待之后的动画效果即将播放了再显示回来
		entry.view.setVisible(false);
		if (view.rootContainer.getLeft() != null) { // 如果此时没有已经显示了的面板
			// 暂时移除默认的侧边栏和主页栏
			view.rootContainer.setCenter(null);
			view.rootContainer.setLeft(null);
		}
		// 向主页栏添加面板
		view.rootContainer.setCenter(entry.view);
		Animation animation = generateAnimation(entry.view, false);
		animation.play();
		entry.view.setVisible(true);
	}

	private void hideCurrent() {
		if (panels.isEmpty()) {
			return;
		}
		PanelEntry entry = panels.poll();
		if (panels.size() <= 0) {
			view.sidebarPane.setVisible(false);
			view.contentPane.setVisible(false);
			view.rootContainer.setLeft(view.sidebarPane);
			view.rootContainer.setCenter(view.contentPane);
			Animation fallback = fallbackAnimation(view.sidebarPane, view.contentPane);
			fallback.play();
			view.sidebarPane.setVisible(true);
			view.contentPane.setVisible(true);
		} else { // 如果此时存在多层叠加(逻辑上的)着的面板
			PanelEntry previous = panels.element();
			Animation animation = generateAnimation(entry.view, true);
			animation.setOnFinished(event -> {
				// 设置为下一层的面板
				previous.view.setVisible(false);
				view.rootContainer.setCenter(previous.view);
				Animation fallback = fallbackAnimation(previous.view);
				fallback.play();
				previous.view.setVisible(true);
			});
			animation.play();
		}
	}

	private ParallelTransition generateAnimation(Region pane, boolean reverse) {
		// 移动动画
		TranslateTransition tran = new TranslateTransition(Duration.millis(200), pane);
		double fromX = (view.contentPane.getLayoutX() + view.contentPane.getWidth() + view.sidebarPane.getWidth()) / 15;
		double toX = view.sidebarPane.getLayoutX();
		tran.setFromX(reverse ? toX : fromX);
		tran.setToX(reverse ? fromX : toX);

		// 渐变动画
		FadeTransition fade = new FadeTransition(Duration.millis(200), pane);
		fade.setFromValue(reverse ? pane.getOpacity() : 0);
		fade.setToValue(reverse ? 0 : pane.getOpacity());

		ParallelTransition parallel = new ParallelTransition(tran, fade);
		return parallel;
	}

	private ParallelTransition fallbackAnimation(Region... panes) {
		ParallelTransition parallel = new ParallelTransition();
		for (Region pane : panes) {
			FadeTransition fade = new FadeTransition(Duration.millis(200), pane);
			fade.setFromValue(0);
			fade.setToValue(pane.getOpacity());
			parallel.getChildren().add(fade);
		}
		return parallel;
	}

	private static class PanelEntry {

		private final Panel model;
		private final Region view;

		private PanelEntry(Panel _model, Region _view) {
			model = _model;
			view = _view;
		}
	}
}
