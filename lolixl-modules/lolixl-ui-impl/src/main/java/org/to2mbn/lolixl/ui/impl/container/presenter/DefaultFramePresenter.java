package org.to2mbn.lolixl.ui.impl.container.presenter;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Parent;
import javafx.scene.layout.Background;
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

	public void initialize() throws IOException {
		super.initialize(LOCATION_OF_FXML);
	}

	/**
	 * 需要在JavaFX线程下运行
	 *
	 * @param background
	 */
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
		return panels.stream()
				.map(entry -> entry.model)
				.toArray(Panel[]::new);
	}

	/**
	 * 需要在JavaFX线程下运行
	 *
	 * @param titleBar
	 */
	public void setTitleBar(Parent titleBar) {
		preCheck(titleBar);
		if (view.titleBarPane != null) {
			view.titleBarPane.setCenter(titleBar);
		}
	}

	/**
	 * 需要在JavaFX线程下运行
	 *
	 * @param sidebar
	 */
	public void setSidebar(Parent sidebar) {
		preCheck(sidebar);
		if (view.sidebarPane != null) {
			view.sidebarPane.setCenter(sidebar);
		}
	}

	/**
	 * 需要在JavaFX线程下运行
	 *
	 * @param content
	 */
	public void setContent(Parent content) {
		preCheck(content);
		if (view.contentPane != null) {
			view.contentPane.setCenter(content);
		}
	}

	private <T> void preCheck(T obj) {
		Objects.requireNonNull(obj);
		FXUtils.checkFxThread();
	}

	private synchronized void displayPanelEntry(Panel model) {
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

	private synchronized void hideCurrent() {
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
					view.rootContainer.setCenter(previous.view)
			);
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

	private static class PanelEntry {
		private final Panel model;
		private final Parent view;

		public PanelEntry(Panel _model, Parent _view) {
			model = _model;
			view = _view;
		}
	}
}
