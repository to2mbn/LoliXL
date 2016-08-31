package org.to2mbn.lolixl.ui.impl.pages.home;

import static org.to2mbn.lolixl.utils.FXUtils.checkFxThread;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.core.game.auth.AuthenticationProfileManager;
import org.to2mbn.lolixl.ui.Presenter;
import org.to2mbn.lolixl.ui.impl.panel.PanelImpl;
import org.to2mbn.lolixl.ui.impl.panel.PanelView;
import org.to2mbn.lolixl.ui.panel.Panel;
import org.to2mbn.lolixl.ui.panel.SidebarPanelDisplayService;
import org.to2mbn.lolixl.utils.FunctionInterpolator;
import org.to2mbn.lolixl.utils.GlobalVariables;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ObservableDoubleValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.util.Duration;

@Service({ SidebarPanelDisplayService.class, LeftSidebarPresenter.class })
@Component(immediate = true)
public class LeftSidebarPresenter extends Presenter<LeftSidebarView> implements SidebarPanelDisplayService {

	private static final String FXML_LOCATION = "fxml/org.to2mbn.lolixl.ui.home/left_sidebar.fxml";

	private static final String CSS_CLASS_PANEL_SHOWN = "xl-left-sidebar-sidebar-container-shown";
	private static final String CSS_CLASS_PANEL_HIDDEN = "xl-left-sidebar-sidebar-container-hidden";

	/*
	 * 关于这个sidebar panel的动画
	 * 
	 * 打开一个panel时，若当前无打开panel，则该panel从左侧滑出。
	 * 关闭一个panel时，则逆转上述过程。
	 * 若打开一个panel时，已有其它panel在显示，
	 * 则之前在显示的panel以1.5倍速度执行关闭动画，
	 * 之后，要打开的panel以1.5倍速度执行打开动画。
	 */

	private double panelAnimationDuration = 300.0;
	private double sidebarPanelWidth = 250.0;
	private double doubleOperationTimeMultiplier = 2.0 / 3.0;

	@Reference(target = GlobalVariables.ANIMATION_TIME_MULTIPLIER)
	private ObservableDoubleValue animationTimeMultiplier;

	private PanelView currentPanel;
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
		checkFxThread();
		return new PanelImpl(this::showPanel, this::closePanel);
	}

	@Override
	public Optional<Panel> getCurrent() {
		checkFxThread();
		return Optional.ofNullable(currentPanel).map(view -> view.model);
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

	private void showPanel(Panel panel) {
		if (currentPanel == null) {
			currentPanel = new PanelView(panel);
			view.sidebarContainer.setContent(currentPanel);
			showPanel(panelAnimationDuration * animationTimeMultiplier.get(), false, null);
		} else {
			showPanel(panelAnimationDuration * animationTimeMultiplier.get() * doubleOperationTimeMultiplier, true, () -> {
				currentPanel = new PanelView(panel);
				view.sidebarContainer.setContent(currentPanel);
				showPanel(panelAnimationDuration * animationTimeMultiplier.get() * doubleOperationTimeMultiplier, false, null);
			});
		}
	}

	private void closePanel(Panel panel) {
		if (currentPanel.model == panel) {
			showPanel(panelAnimationDuration * animationTimeMultiplier.get(), true, () -> {
				currentPanel = null;
				view.sidebarContainer.setContent(null);
			});
		}
	}

	private void showPanel(double t, boolean reverse, Runnable callback) {
		addNewAnimation(() -> {
			double endWidth = reverse ? 0.0 : sidebarPanelWidth;
			double startOpacity = reverse ? 1.0 : 0.0;
			double endOpacity = reverse ? 0.0 : 1.0;

			List<KeyValue> keyValues = new ArrayList<>();
			keyValues.add(new KeyValue(view.sidebarContainer.prefWidthProperty(), endWidth, FunctionInterpolator.S_CURVE));
			if (currentPanel != null) {
				for (Node node : getPanelViewAnimationNodes(currentPanel)) {
					node.setOpacity(startOpacity);
					keyValues.add(new KeyValue(node.opacityProperty(), endOpacity, FunctionInterpolator.S_CURVE));
				}
			}

			Timeline timeline = new Timeline(new KeyFrame(Duration.millis(t), keyValues.toArray(new KeyValue[keyValues.size()])));
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

	private List<Node> getPanelViewAnimationNodes(PanelView panel) {
		List<Node> nodes = new ArrayList<>();
		nodes.add(panel.previousButton);
		nodes.add(panel.iconView);
		nodes.add(panel.titleLabel);
		nodes.add(panel.panelContentContainer);
		return nodes;
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
