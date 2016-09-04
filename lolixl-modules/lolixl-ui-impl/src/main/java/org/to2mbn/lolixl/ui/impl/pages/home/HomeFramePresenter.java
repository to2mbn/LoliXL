package org.to2mbn.lolixl.ui.impl.pages.home;

import static org.to2mbn.lolixl.utils.FXUtils.checkFxThread;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.ui.Presenter;
import org.to2mbn.lolixl.ui.impl.panel.PanelImpl;
import org.to2mbn.lolixl.ui.impl.panel.PanelView;
import org.to2mbn.lolixl.ui.panel.Panel;
import org.to2mbn.lolixl.ui.panel.PanelDisplayService;
import org.to2mbn.lolixl.ui.theme.background.BackgroundService;
import org.to2mbn.lolixl.utils.FXUtils;
import org.to2mbn.lolixl.utils.FunctionInterpolator;
import org.to2mbn.lolixl.utils.GlobalVariables;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableDoubleValue;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

@Service({ PanelDisplayService.class, HomeFramePresenter.class })
@Component(immediate = true)
public class HomeFramePresenter extends Presenter<HomeFrameView> implements PanelDisplayService {

	private double panelAnimationDuration = 300.0;

	@Reference(target = GlobalVariables.ANIMATION_TIME_MULTIPLIER)
	private ObservableDoubleValue animationTimeMultiplier;

	@Reference
	private LeftSidebarPresenter sidebarPresenter;

	@Reference
	private HomeContentPresenter homeContentPresenter;

	@Reference
	private BackgroundService backgroundService;

	@Reference
	private TitleBarPresenter titleBarPresenter;

	/*
	 * 动画伪代码：
		shown : stack<panel>
		pending_animations : queue<function>
		current_animation : optional<animation>
		
		show(p : panel) : void
		  if current_animation exists
		    submit this to pending_animations
		  else
		    play show animation
		
		hide(p : panel)
		  if current_animation exists
		    if p takes part in current_animation
		      if p is being hidden
		        do nothing
		      else if p is being shown
		        cancel current_animation and play close animation
		      else
		        submit this to pending_animations
		    else
		      // p hasn't taken part in current_animation(not in display)
		      directly remove p from shown
		  else if shown.peek() = p // p is in display
		    play hide animation
		  else
		    directly remove p from shown
	 */

	private static final int UNDEFINED = 0;
	private static final int SHOWING = 1;
	private static final int HIDING = 2;
	private LinkedList<PanelEntry> panels = new LinkedList<>();
	private LinkedList<Runnable> pendingAnimations = new LinkedList<>();
	private Animation currentAnimation = null;
	private int currentAnimationType = UNDEFINED;

	private Runnable onEscPress = () -> {
		if (getCurrent().isPresent()) {
			getCurrent().get().hide();
		} else if (sidebarPresenter.getCurrent().isPresent()) {
			sidebarPresenter.getCurrent().get().hide();
		}
	};

	private BlurArea areaSidebar;
	private BlurArea areaTitleBar;

	@Activate
	public void active(ComponentContext compCtx) {
		super.active();
	}

	@Override
	protected void initializePresenter() {
		view.bindTitleBar(titleBarPresenter.getView().rootContainer);
		view.homeContentPane.setLeft(sidebarPresenter.getView().rootContainer);
		view.homeContentPane.setCenter(homeContentPresenter.getView().rootContainer);
		view.contentPane.getChildren().add(view.homeContentPane);

		view.rootContainer.sceneProperty().addListener((dummy, oldVal, newVal) -> {
			if (oldVal != null) {
				oldVal.getAccelerators().remove(KeyCode.ESCAPE, onEscPress);
			}
			if (newVal != null) {
				newVal.getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE), onEscPress);
			}
		});

		areaSidebar = new BlurArea(sidebarPresenter.getView().mainContentContainer);
		areaTitleBar = new BlurArea(titleBarPresenter.getView().rootContainer);
		view.backgroundPane = new BlurBackgroundPane(() -> {
			List<BlurArea> areas = new ArrayList<>();
			areas.add(areaTitleBar);
			for (Node node : view.contentPane.getChildren()) {
				if (node == view.homeContentPane) {
					areas.add(areaSidebar);
				} else if (node instanceof Region) {
					areas.add(new BlurArea((Region) node));
				}
			}
			return areas;
		}, backgroundService.getCurrentBackground());
		view.rootContainer.getChildren().add(0, view.backgroundPane);
		FXUtils.bindPrefSize(view.backgroundPane, view.rootContainer);
		FXUtils.setSizeToPref(view.backgroundPane);
		updatePanelsBlur();
	}

	@Override
	protected String getFxmlLocation() {
		return "fxml/org.to2mbn.lolixl.ui.home/frame.fxml";
	}

	@Override
	public Panel newPanel() {
		checkFxThread();
		return new PanelImpl(this::showPanel, this::hidePanel);
	}

	@Override
	public Optional<Panel> getCurrent() {
		checkFxThread();
		return Optional.ofNullable(panels.peek())
				.map(p -> p.model);
	}

	@Override
	public Panel[] getOpenedPanels() {
		checkFxThread();
		return panels.stream()
				.map(entry -> entry.model)
				.toArray(Panel[]::new);
	}

	public void updateAreaPosition() {
		areaSidebar.absPosChangeNotfier.notifyChanged();
		areaTitleBar.absPosChangeNotfier.notifyChanged();
	}

	private void showPanel(Panel panel) {
		checkFxThread();
		Objects.requireNonNull(panel);

		if (currentAnimation == null) {
			playShowAnimation(panel);
		} else {
			pendingAnimations.add(() -> showPanel(panel));
		}
	}

	private void hidePanel(Panel panel) {
		checkFxThread();
		Objects.requireNonNull(panel);

		PanelEntry entry = null;
		for (PanelEntry e : panels) {
			if (e.model == panel) {
				entry = e;
				break;
			}
		}
		if (entry == null) {
			return;
		}

		boolean isFirst = panels.peek() == entry;
		if (currentAnimation != null) {
			boolean isSecond = panels.size() > 1 && panels.get(1) == entry;
			if (isFirst || isSecond) {
				if (isFirst) {
					if (currentAnimationType == HIDING) {
						// do nothing
					} else if (currentAnimationType == SHOWING) {
						double progress = currentAnimation.getCurrentTime().toMillis() / currentAnimation.getTotalDuration().toMillis();
						currentAnimation.stop();
						playHideAnimation(progress);
					} else {
						throw new IllegalStateException("Illegal animation type: " + currentAnimationType);
					}
				} else {
					pendingAnimations.add(() -> hidePanel(panel));
				}
			} else {
				panels.remove(entry);
			}
		} else if (isFirst) {
			playHideAnimation(Double.NaN);
		} else {
			panels.remove(entry);
		}
	}

	private void onAnimationFinished() {
		currentAnimation = null;
		currentAnimationType = UNDEFINED;
		Runnable next = pendingAnimations.poll();
		if (next != null) {
			next.run();
		}
	}

	private void playShowAnimation(Panel toShow) {
		Region lower = panels.isEmpty() ? view.homeContentPane : panels.peek().view;
		PanelEntry entry = new PanelEntry(toShow);
		panels.push(entry);
		Region upper = entry.view;

		turnOnSpeedCache(upper);
		turnOnSpeedCache(lower);

		view.contentPane.getChildren().add(upper);
		view.contentPane.layout();
		updatePanelsBlur();

		List<KeyValue> keyValues = new ArrayList<>();

		// opacity
		upper.setOpacity(0.0);
		keyValues.add(new KeyValue(upper.opacityProperty(), 1.0));
		keyValues.add(new KeyValue(lower.opacityProperty(), 0.0));

		// location
		Rectangle clip = new Rectangle(upper.getWidth(), upper.getHeight());
		ChangeListener<? super Number> onTranslateXChange = (dummy, oldVal, newVal) -> {
			clip.setWidth(Math.max(view.rootContainer.getWidth() - newVal.doubleValue(), 0.0));
		};
		upper.setClip(clip);

		upper.translateXProperty().addListener(onTranslateXChange);

		upper.setTranslateX(getPanelTranslateEndX());
		keyValues.add(new KeyValue(upper.translateXProperty(), 0.0, FunctionInterpolator.S_CURVE));

		currentAnimation = new Timeline(new KeyFrame(Duration.millis(panelAnimationDuration * animationTimeMultiplier.get()), keyValues.toArray(new KeyValue[keyValues.size()])));

		currentAnimationType = SHOWING;
		currentAnimation.setOnFinished(e -> {
			upper.setClip(null);
			turnOffSpeedCache(upper);
			turnOffSpeedCache(lower);
			upper.translateXProperty().removeListener(onTranslateXChange);
			view.contentPane.getChildren().remove(0);
			updatePanelsBlur();
			onAnimationFinished();
		});
		currentAnimation.play();
	}

	/**
	 * @param showAnimationProgress the progress of the canceled show animation,
	 *            NaN if no show animation is canceled
	 */
	private void playHideAnimation(double showAnimationProgress) {
		boolean canceledShowAnimation = !Double.isNaN(showAnimationProgress);
		Region upper = panels.peek().view;
		Region lower = panels.size() > 1 ? panels.get(1).view : view.homeContentPane;

		turnOnSpeedCache(upper);
		turnOnSpeedCache(lower);

		if (!canceledShowAnimation) {
			view.contentPane.getChildren().add(0, lower);
			lower.setOpacity(0.0);
		}
		updatePanelsBlur();

		List<KeyValue> keyValues = new ArrayList<>();
		keyValues.add(new KeyValue(upper.opacityProperty(), 0.0));
		keyValues.add(new KeyValue(lower.opacityProperty(), 1.0));

		Rectangle clip = new Rectangle(upper.getWidth(), upper.getHeight());
		ChangeListener<? super Number> onTranslateXChange = (dummy, oldVal, newVal) -> {
			clip.setWidth(Math.max(view.rootContainer.getWidth() - newVal.doubleValue(), 0.0));
		};
		upper.setClip(clip);

		upper.translateXProperty().addListener(onTranslateXChange);

		// location
		keyValues.add(new KeyValue(upper.translateXProperty(), getPanelTranslateEndX(), FunctionInterpolator.S_CURVE));

		currentAnimation = new Timeline(new KeyFrame(Duration.millis(panelAnimationDuration * animationTimeMultiplier.get() * (canceledShowAnimation ? showAnimationProgress : 1.0)), keyValues.toArray(new KeyValue[keyValues.size()])));

		currentAnimationType = HIDING;
		currentAnimation.setOnFinished(e -> {
			upper.setClip(null);
			turnOffSpeedCache(upper);
			turnOffSpeedCache(lower);
			upper.translateXProperty().removeListener(onTranslateXChange);
			panels.pop();
			view.contentPane.getChildren().remove(1);
			updatePanelsBlur();
			onAnimationFinished();
		});
		currentAnimation.play();
	}

	private double getPanelTranslateEndX() {
		return view.rootContainer.getWidth() / 4.0;
	}

	private void updatePanelsBlur() {
		view.backgroundPane.updateArea();
	}

	private void turnOnSpeedCache(Region node) {
		node.setCache(true);
		node.setCacheHint(CacheHint.SPEED);
	}

	private void turnOffSpeedCache(Region node) {
		node.setCache(false);
		node.setCacheHint(CacheHint.DEFAULT);
	}

	private class PanelEntry {

		Panel model;
		PanelView view;

		private PanelEntry(Panel model) {
			this.model = model;
			this.view = new PanelView(model);
			StackPane.setAlignment(this.view, Pos.TOP_LEFT);
		}
	}
}
