package org.to2mbn.lolixl.ui.impl.container.presenter;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Parent;
import javafx.scene.layout.Background;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.felix.scr.annotations.Component;
import org.to2mbn.lolixl.ui.BackgroundService;
import org.to2mbn.lolixl.ui.Panel;
import org.to2mbn.lolixl.ui.PanelDisplayService;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.component.model.PanelImpl;
import org.to2mbn.lolixl.ui.impl.component.view.ContentPanelView;
import org.to2mbn.lolixl.ui.impl.container.view.DefaultFrameView;
import org.to2mbn.lolixl.utils.FXUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class DefaultFramePresenter extends Presenter<DefaultFrameView> implements BackgroundService, PanelDisplayService {
	private static final String LOCATION_OF_FXML = "/ui/fxml/container/default_frame.fxml";

	private final Queue<PanelEntry> panels = new ConcurrentLinkedQueue<>();

	// for draggable:
	private double lastDragX, lastDragY;
	private boolean isDragging = false;
	// for resizeable:
	private double lastResizeX, lastResizeY;
	private Stage stage;

	@Override
	public void postInitialize() {
		makeDraggable();
		makeResizeable();
	}

	@Override
	protected String getFxmlLocation() {
		return LOCATION_OF_FXML;
	}

	@Override
	public void setBackground(Background background) {
		preCheck(background);
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
		PanelEntry entry = panels.poll();
		return entry != null ? Optional.of(entry.model) : Optional.empty();
	}

	@Override
	public Panel[] getOpenedPanels() {
		return panels.stream().map(entry -> entry.model).toArray(Panel[]::new);
	}

	/**
	 * 需要在JavaFX线程下运行
	 */
	public void setTitleBar(Parent titleBar) {
		preCheck(titleBar);
		if (view.titleBarPane != null) {
			view.titleBarPane.getChildren().add(titleBar);
		}
	}

	/**
	 * 需要在JavaFX线程下运行
	 */
	public void setSidebar(Parent sidebar) {
		preCheck(sidebar);
		if (view.sidebarPane != null) {
			view.sidebarPane.getChildren().add(sidebar);
		}
	}

	/**
	 * 需要在JavaFX线程下运行
	 */
	public void setContent(Parent content) {
		preCheck(content);
		if (view.contentPane != null) {
			view.contentPane.getChildren().add(content);
		}
	}

	public void setStage(Stage _stage) {
		stage = _stage;
	}

	private <T> void preCheck(T obj) {
		Objects.requireNonNull(obj);
		FXUtils.checkFxThread();
	}

	private void displayPanelEntry(Panel model) {
		PanelEntry entry;
		try {
			entry = new PanelEntry(model, new ContentPanelView(model));
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
		Animation animation = generateAnimation(entry.view, true);
		if (panels.size() == 1) {
			animation.setOnFinished(event -> {
				// 先隐藏面板
				entry.view.setVisible(false);
				// 把先前隐藏的侧边栏和主页栏加回来
				view.rootContainer.setLeft(view.sidebarPane);
				view.rootContainer.setCenter(view.contentPane);
			});
		} else { // 如果此时存在多层叠加(逻辑上的)着的面板
			PanelEntry previous = panels.element();
			animation.setOnFinished(event ->
					// 直接设置为下一层的面板
					view.rootContainer.setCenter(previous.view));
		}
		animation.play();
	}

	private ParallelTransition generateAnimation(Parent pane, boolean reverse) {
		// 移动动画
		TranslateTransition tran = new TranslateTransition(Duration.seconds(1), pane);
		double fromX = (view.contentPane.getLayoutX() + view.contentPane.getWidth() + view.sidebarPane.getWidth()) / 5;
		double toX = view.sidebarPane.getLayoutX();
		tran.setFromX(reverse ? toX : fromX);
		tran.setToX(reverse ? fromX : toX);

		// 渐变动画
		FadeTransition fade = new FadeTransition(Duration.seconds(1), pane);
		fade.setFromValue(reverse ? 0.5 : 1);
		fade.setToValue(reverse ? 1 : 0.5);

		ParallelTransition parallel = new ParallelTransition(tran, fade);
		parallel.setCycleCount(Animation.INDEFINITE);
		return parallel;
	}


	private void makeDraggable() {
		view.titleBarPane.setOnMousePressed(event -> {
			if (!checkIfOnEdge(event.getSceneY(), event.getSceneY())) {
				lastDragX = event.getSceneX();
				lastDragY = event.getSceneY();
				isDragging = true;
			}
		});
		view.titleBarPane.setOnMouseDragged(event -> {
			if (isDragging) {
				stage.setX(event.getScreenX() - lastDragX);
				stage.setY(event.getScreenY() - lastDragY);
			}
		});
		view.titleBarPane.setOnMouseReleased(event -> {
			isDragging = false;
		});
	}

	private void makeResizeable() {
		// TODO: no need for ubuntu
		view.shadowContainer.setOnMousePressed(event -> {
			if (!isDragging && checkIfOnEdge(event.getSceneX(), event.getSceneY())) {
				lastResizeX = event.getSceneX();
				lastResizeY = event.getSceneY();
			}
		});
		view.shadowContainer.setOnMouseDragged(event -> {
			if (!isDragging && !checkMinSize()){
				double height = view.shadowContainer.getHeight();
				double width = view.shadowContainer.getWidth();
				view.shadowContainer.resize(width + event.getSceneX() - lastResizeX, height + event.getSceneY() - lastResizeY);
			}
		});
	}

	private boolean checkIfOnEdge(double x, double y) {
		if (x >= 3 && x <= view.shadowContainer.getWidth() - 3) {
			return (y >= 3 && y <= 12) || (y >= view.shadowContainer.getHeight() - 12 && y <= view.shadowContainer.getHeight() - 3);
		} else if (y >= 3 && y <= view.shadowContainer.getHeight() - 3) {
			return (x >= 3 && x <= 12) || (x >= view.shadowContainer.getWidth() - 12 && x <= view.shadowContainer.getWidth() - 3);
		}
		return false;
	}

	private boolean checkMinSize() {
		return view.shadowContainer.getHeight() <= view.shadowContainer.getMinHeight()
				|| view.shadowContainer.getWidth() <= view.shadowContainer.getMinWidth();
	}

	private static class PanelEntry {
		private final Panel model;
		private final Parent view;

		private PanelEntry(Panel _model, Parent _view) {
			model = _model;
			view = _view;
		}
	}
}
