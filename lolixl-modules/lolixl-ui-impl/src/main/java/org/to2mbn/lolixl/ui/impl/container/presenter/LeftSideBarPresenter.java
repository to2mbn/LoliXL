package org.to2mbn.lolixl.ui.impl.container.presenter;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.core.game.auth.AuthenticationProfileManager;
import org.to2mbn.lolixl.ui.Panel;
import org.to2mbn.lolixl.ui.SideBarPanelDisplayService;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.component.model.PanelImpl;
import org.to2mbn.lolixl.ui.impl.component.view.panel.PanelView;
import org.to2mbn.lolixl.ui.impl.container.view.LeftSidebarView;
import org.to2mbn.lolixl.utils.FunctionInterpolator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

@Service({ SideBarPanelDisplayService.class, LeftSideBarPresenter.class })
@Component(immediate = true)
public class LeftSideBarPresenter extends Presenter<LeftSidebarView> implements SideBarPanelDisplayService {

	private static final String FXML_LOCATION = "fxml/org.to2mbn.lolixl.ui.home/left_sidebar.fxml";

	private static final String CSS_CLASS_PANEL_SHOWN = "xl-left-sidebar-sidebar-container-shown";
	private static final String CSS_CLASS_PANEL_HIDDEN = "xl-left-sidebar-sidebar-container-hidden";

	/*
	 * 关于这个sidebar panel的动画
	 * 
	 * 打开一个panel时，若当前无打开panel，则该panel从左侧滑出。
	 * 关闭一个panel时，则逆转上述过程。
	 * 若打开一个panel时，已有其它panel在显示，
	 * 则之前在显示的panel以2倍速度执行关闭动画，
	 * 之后，要打开的panel以2倍速度执行打开动画。
	 */

	private double panelAnimationDuration = 300.0;
	private double sidebarPanelWidth = 250.0;

	private Panel currentPanel;
	private Queue<Runnable> pendingAnimations = new LinkedList<>();

	@Reference
	private AuthenticationProfileManager authProfileManager;

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
		return Optional.ofNullable(currentPanel);
	}

	@Override
	protected void initializePresenter() {
		setSidebarPanelStateCssClass(CSS_CLASS_PANEL_HIDDEN);
		view.sidebarContainer.contentProperty().addListener((observable, oldVal, newVal) -> {
			if (oldVal instanceof Region) {
				((Region) oldVal).prefWidthProperty().unbind();
				((Region) oldVal).prefHeightProperty().unbind();
			}
			if (newVal instanceof Region) {
				((Region) newVal).prefWidthProperty().bind(view.sidebarContainer.widthProperty());
				((Region) newVal).prefHeightProperty().bind(view.sidebarContainer.heightProperty());
			}
		});
	}

	private void showNewPanel(Panel panel) {
		if (currentPanel == null) {
			currentPanel = panel;
			view.sidebarContainer.setContent(new PanelView(panel));
			showPanel(panelAnimationDuration, false, null);
		} else {
			showPanel(panelAnimationDuration / 2.0, true, () -> {
				currentPanel = panel;
				view.sidebarContainer.setContent(new PanelView(panel));
				showPanel(panelAnimationDuration / 2.0, false, null);
			});
		}
	}

	private void closeCurrentPanel() {
		if (currentPanel != null) {
			showPanel(panelAnimationDuration, true, () -> {
				currentPanel = null;
				view.sidebarContainer.setContent(null);
			});
		}
	}

	private void showPanel(double t, boolean reverse, Runnable callback) {
		addNewAnimation(() -> {
			double endValue = reverse ? 0.0 : sidebarPanelWidth;
			Timeline timeline = new Timeline(new KeyFrame(Duration.millis(t),
					new KeyValue(view.sidebarContainer.prefWidthProperty(), endValue, FunctionInterpolator.S_CURVE)));
			setSidebarPanelStateCssClass(null);
			timeline.setOnFinished(event -> {
				setSidebarPanelStateCssClass(reverse ? CSS_CLASS_PANEL_HIDDEN : CSS_CLASS_PANEL_SHOWN);
				if (callback != null)
					callback.run();
				onAnimationFinished();
			});
			timeline.play();
		});
	}

	private void addNewAnimation(Runnable animation) {
		pendingAnimations.offer(animation);
		if (pendingAnimations.size() == 1) {
			animation.run();
		}
	}

	private void onAnimationFinished() {
		pendingAnimations.poll();
		Runnable next = pendingAnimations.peek();
		if (next != null) {
			next.run();
		}
	}

	private void setSidebarPanelStateCssClass(String cssClass) {
		ObservableList<String> styleClasses = view.sidebarContainer.getStyleClass();
		styleClasses.remove(CSS_CLASS_PANEL_SHOWN);
		styleClasses.remove(CSS_CLASS_PANEL_HIDDEN);
		if (cssClass != null) {
			styleClasses.add(cssClass);
		}
	}
}
