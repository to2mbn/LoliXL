package org.to2mbn.lolixl.ui.impl.container.presenter;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.util.Duration;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.ui.Panel;
import org.to2mbn.lolixl.ui.SideBarAlertService;
import org.to2mbn.lolixl.ui.SideBarPanelDisplayService;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.component.model.PanelImpl;
import org.to2mbn.lolixl.ui.impl.container.view.DefaultSidebarView;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

@Service({ SideBarPanelDisplayService.class, SideBarAlertService.class, DefaultSideBarPresenter.class })
@Component(immediate = true)
public class DefaultSideBarPresenter extends Presenter<DefaultSidebarView> implements SideBarPanelDisplayService, SideBarAlertService {

	private static final String FXML_LOCATION = "/ui/fxml/container/default_side_bar.fxml";

	private final List<Tile> alerts = new Vector<>();
	private final Timer timer = new Timer(true);

	private Panel currentPanel;
	private int currentAlertIdx;

	@Activate
	public void active(ComponentContext compCtx) {
		super.active();
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
		alerts.add(alert);
	}

	@Override
	public void removeAlert(Tile alert) {
		Objects.requireNonNull(alert);
		alerts.remove(alert);
	}

	@Override
	protected void initializePresenter() {
		startAlertDisplayWorkCycle();
	}

	private void startAlertDisplayWorkCycle() {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				nextAlert();
			}
		}, 0, 8000); // make period configurable
	}

	private void nextAlert() {
		if (alerts.isEmpty()) {
			return;
		}
		if (currentAlertIdx >= alerts.size()) {
			currentAlertIdx = 0;
		}
		List<Node> children = view.functionalTileBottomContainer.getChildren();
		Tile current = currentAlertIdx <= 0 ? null : alerts.get(currentAlertIdx - 1);
		Tile next = alerts.get(currentAlertIdx);
		if (current != null) {
			Animation out = generateAlertAnimation(current, true);
			out.setOnFinished(event -> children.remove(current));
			out.play();
		}
		Animation in = generateAlertAnimation(next, false);
		next.setVisible(false);
		children.add(next);
		in.play();
		next.setVisible(true);
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
