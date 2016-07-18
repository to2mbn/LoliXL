package org.to2mbn.lolixl.ui.container.presenter;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.lolixl.ui.container.view.DefaultFrameView;
import org.to2mbn.lolixl.ui.service.BackgroundManagingService;
import org.to2mbn.lolixl.ui.service.ContentDisplayService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Component
@Service({DefaultFramePresenter.class, BackgroundManagingService.class, ContentDisplayService.class})
public class DefaultFramePresenter extends Presenter<DefaultFrameView> implements BackgroundManagingService, ContentDisplayService {
	private final ConcurrentLinkedQueue<Pane> contents = new ConcurrentLinkedQueue<>();
	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

	private boolean hidedSidebar = false;
	private Node sidebar = null;

	@Override
	public void initialize(URL fxmlLocation) throws IOException {
		super.initialize(fxmlLocation);
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
	public void displayContent(Pane pane, boolean hideSidebar) {
		Objects.requireNonNull(pane);
		Lock lock = rwLock.writeLock();
		lock.lock();
		try {
			contents.offer(pane);
		} finally {
			lock.unlock();
		}

		Platform.runLater(() -> {
			double plus = hideSidebar ? view.sidebarPane.getWidth() : 0;
			pane.resize(view.contentPane.getWidth() + plus, view.contentPane.getHeight());
			if (hideSidebar) {
				sidebar = view.sidebarPane.getCenter();
				view.sidebarPane.setCenter(null);
			}

			ParallelTransition animation = generateAnimation(pane, false, hideSidebar);
			animation.setOnFinished(event -> {
				view.setContent(pane);
				hidedSidebar = hideSidebar;
			});
			animation.play();
		});
	}

	@Override
	public boolean closeCurrentContent() {
		if (contents.size() <= 1) {
			return false;
		}
		Lock lock = rwLock.writeLock();
		lock.lock();
		try {
			Pane last = contents.poll();
			Pane previous = contents.poll();

			Platform.runLater(() -> {
				ParallelTransition animation = generateAnimation(last, true, hidedSidebar);
				animation.setOnFinished(event -> {
					if (hidedSidebar) {
						view.sidebarPane.setCenter(sidebar);
					}
					view.setContent(previous);
				});
				animation.play();
			});
		} finally {
			lock.unlock();
		}
		return true;
	}

	@Override
	public List<Pane> getAvailableContents() {
		return contents.stream().collect(Collectors.toList());
	}

	private ParallelTransition generateAnimation(Pane pane, boolean reverse, boolean hidedSidebar) {
		TranslateTransition tran = new TranslateTransition(Duration.seconds(1), pane);
		double plus = hidedSidebar ? view.sidebarPane.getWidth() : 0;
		double fromX = (view.contentPane.getLayoutX() + view.contentPane.getWidth() + plus) / 5;
		double toX = hidedSidebar ? view.sidebarPane.getLayoutX() : view.contentPane.getLayoutX();
		tran.setFromX(reverse ? toX : fromX);
		tran.setToX(reverse ? fromX : toX);

		FadeTransition fade = new FadeTransition(Duration.seconds(1), pane);
		fade.setFromValue(1 / (reverse ? 2 : 1));
		fade.setToValue(1 / (reverse ? 1 : 2));

		ParallelTransition parallel = new ParallelTransition(tran, fade);
		parallel.setCycleCount(Animation.INDEFINITE);
		return parallel;
	}
}
