package org.to2mbn.lolixl.ui.impl.container.presenter;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.util.Duration;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.framework.BundleContext;
import org.to2mbn.lolixl.ui.Panel;
import org.to2mbn.lolixl.ui.SideBarAlertService;
import org.to2mbn.lolixl.ui.SideBarPanelDisplayService;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.component.model.PanelImpl;
import org.to2mbn.lolixl.ui.impl.container.view.DefaultSidebarView;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;

@Component
public class DefaultSideBarPresenter extends Presenter<DefaultSidebarView> implements SideBarPanelDisplayService, SideBarAlertService {

	private static final String FXML_LOCATION = "/ui/fxml/container/default_side_bar.fxml";

	private final Deque<Tile> bottomAlerts;

	private Panel currentPanel;

	@Reference(target = "(usage=cpu_compute)")
	private ExecutorService cpuComputePool;

	public DefaultSideBarPresenter(BundleContext ctx) {
		super(ctx);
		ctx.registerService(SideBarPanelDisplayService.class, this, null);
		ctx.registerService(SideBarAlertService.class, this, null);
		bottomAlerts = new ConcurrentLinkedDeque<>();
	}

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
	}

	@Override
	public Panel newPanel() {
		return new PanelImpl(this::showNewPanel, this::closeCurrentPanel);
	}

	@Override
	public Optional<Panel> getCurrent() {
		return currentPanel != null ? Optional.of(currentPanel) : Optional.empty();
	}

	@Override
	public void addAlert(Tile alert) {
		Objects.requireNonNull(alert);
		bottomAlerts.addLast(alert);
	}

	@Override
	public void removeAlert(Tile alert) {
		Objects.requireNonNull(alert);
		bottomAlerts.remove(alert);
	}

	@Override
	public void postInitialize() {
		startAlertDisplayWorkCycle();
	}

	private void startAlertDisplayWorkCycle() {
		cpuComputePool.execute(() -> {
			while (true) {
				ObservableList<Node> children = view.functionalTileBottomContainer.getChildren();
				Iterator<Tile> paddingAlerts = bottomAlerts.iterator();
				Tile last = null;
				while (paddingAlerts.hasNext()) {
					Tile current = paddingAlerts.next();
					Animation in = generateAlertAnimation(current, false);
					current.setVisible(false);
					children.add(current);
					if (last != null) {
						Animation out = generateAlertAnimation(last, true);
						int lastIdx = children.indexOf(last);
						out.setOnFinished(event -> children.remove(lastIdx));
						out.play();
					}
					current.setVisible(true);
					in.play();

					last = current;

					try {
						Thread.currentThread().sleep(5000L); // TODO: make it configurable
					} catch (InterruptedException e) {
						// ignore
					}
				}
			}
		});
	}

	private Animation generateAlertAnimation(Tile alert, boolean goOff) {
		double y = view.functionalTileBottomContainer.getLayoutY();
		double height = view.functionalTileBottomContainer.getHeight();
		TranslateTransition tran = new TranslateTransition(Duration.millis(300), alert);
		if (goOff) {
			tran.setFromY(alert.getLayoutY());
			tran.setToY(y - alert.getHeight() - 10);
		} else {
			tran.setFromY(y + height);
			tran.setToY(height / 2);
		}

		FadeTransition fade = new FadeTransition(Duration.millis(300), alert);
		if (goOff) {
			fade.setFromValue(alert.getOpacity());
			fade.setToValue(0);
		} else {
			fade.setFromValue(0);
			fade.setToValue(alert.getOpacity());
		}

		ParallelTransition parallel = new ParallelTransition(tran, fade);
		parallel.setCycleCount(Animation.INDEFINITE);
		return parallel;
	}

	private void showNewPanel(Panel panel) {
		Objects.requireNonNull(panel);
		if (currentPanel != null) {
			currentPanel.hide();
		}
		currentPanel = panel;
		Parent pane = panel.contentProperty().get();
		pane.setVisible(false);
		view.sidebarContainer.getChildren().add(pane);
		Animation animation = generateAnimation(pane);
		pane.setVisible(true);
		animation.play();
	}

	private void closeCurrentPanel() {
		view.sidebarContainer.getChildren().clear();
		currentPanel = null;
	}

	private Animation generateAnimation(Parent pane) {
		TranslateTransition tran = new TranslateTransition(Duration.seconds(1), pane);
		double from = view.mainContentContainer.getLayoutX();
		tran.setFromX(from);
		tran.setToX(from + view.mainContentContainer.getWidth());
		return tran;
	}
}
