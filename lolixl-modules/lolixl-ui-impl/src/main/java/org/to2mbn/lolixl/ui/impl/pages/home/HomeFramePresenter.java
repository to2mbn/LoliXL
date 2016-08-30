package org.to2mbn.lolixl.ui.impl.pages.home;

import static org.to2mbn.lolixl.utils.FXUtils.checkFxThread;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.Observable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
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
import org.to2mbn.lolixl.utils.FunctionInterpolator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service({ PanelDisplayService.class, HomeFramePresenter.class })
@Component(immediate = true)
public class HomeFramePresenter extends Presenter<HomeFrameView> implements PanelDisplayService {

	private static final String FXML_LOCATION = "fxml/org.to2mbn.lolixl.ui.home/frame.fxml";

	private double panelAnimationDuration = 300.0;

	@Reference
	private LeftSidebarPresenter sideBarPresenter;

	@Reference
	private HomeContentPresenter homeContentPresenter;

	@Reference
	private BackgroundService backgroundService;

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

	@Activate
	public void active(ComponentContext compCtx) {
		super.active();
	}

	@Override
	protected void initializePresenter() {
		view.homeContentPane.setLeft(sideBarPresenter.getView().rootContainer);
		view.homeContentPane.setCenter(homeContentPresenter.getView().rootContainer);
		view.contentPane.getChildren().add(view.homeContentPane);

		view.rootContainer.sceneProperty().addListener((Observable dummy) -> {
			Scene scene = view.rootContainer.getScene();
			if (scene != null) {
				view.rootContainer.getScene().getAccelerators().put(
						new KeyCodeCombination(KeyCode.ESCAPE),
						() -> {
							if (getCurrent().isPresent()) {
								getCurrent().get().hide();
							} else if (sideBarPresenter.getCurrent().isPresent()) {
								sideBarPresenter.getCurrent().get().hide();
							}
						});
			}
		});

		view.rootContainer.backgroundProperty().bind(backgroundService.getCurrentBackground());
	}

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
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

		view.contentPane.getChildren().add(upper);

		List<KeyValue> keyValues = new ArrayList<>();

		// opacity
		upper.setOpacity(0.0);
		keyValues.add(new KeyValue(upper.opacityProperty(), 1.0));
		keyValues.add(new KeyValue(lower.opacityProperty(), 0.0));

		// location
		upper.setTranslateX(getPanelTranslateEndX());
		keyValues.add(new KeyValue(upper.translateXProperty(), 0.0, FunctionInterpolator.S_CURVE));

		currentAnimation = new Timeline(new KeyFrame(Duration.millis(panelAnimationDuration), keyValues.toArray(new KeyValue[keyValues.size()])));

		currentAnimationType = SHOWING;
		currentAnimation.setOnFinished(e -> {
			view.contentPane.getChildren().remove(0);
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

		if (!canceledShowAnimation) {
			view.contentPane.getChildren().add(0, lower);
			lower.setOpacity(0.0);
		}

		List<KeyValue> keyValues = new ArrayList<>();
		keyValues.add(new KeyValue(upper.opacityProperty(), 0.0));
		keyValues.add(new KeyValue(lower.opacityProperty(), 1.0));

		// location
		keyValues.add(new KeyValue(upper.translateXProperty(), getPanelTranslateEndX(), FunctionInterpolator.S_CURVE));

		currentAnimation = new Timeline(new KeyFrame(Duration.millis(panelAnimationDuration * (canceledShowAnimation ? showAnimationProgress : 1.0)), keyValues.toArray(new KeyValue[keyValues.size()])));

		currentAnimationType = HIDING;
		currentAnimation.setOnFinished(e -> {
			panels.pop();
			view.contentPane.getChildren().remove(1);
			onAnimationFinished();
		});
		currentAnimation.play();
	}

	private double getPanelTranslateEndX() {
		return view.rootContainer.getWidth() / 8.0;
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
