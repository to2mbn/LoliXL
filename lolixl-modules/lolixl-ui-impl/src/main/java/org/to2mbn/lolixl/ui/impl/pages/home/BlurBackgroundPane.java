package org.to2mbn.lolixl.ui.impl.pages.home;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.to2mbn.lolixl.ui.impl.util.BlurArea;
import org.to2mbn.lolixl.utils.FXUtils;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class BlurBackgroundPane extends StackPane {

	private static final String PROPERTY_OPACITY = "org.to2mbn.lolixl.ui.blurBackground.opacityProperty";

	private double gaussianBlurRadius = 10.0;

	private Supplier<List<BlurArea>> blurArea;
	private ObservableValue<Background> background;
	private List<ScrollPane> blurLayers = new ArrayList<>();

	public BlurBackgroundPane(Supplier<List<BlurArea>> blurArea, ObservableValue<Background> background) {
		this.blurArea = blurArea;
		this.background = background;
		backgroundProperty().bind(background);
	}

	public void updateArea() {
		List<BlurArea> areas = blurArea.get();
		int difference = areas.size() - blurLayers.size();
		if (difference != 0) {
			if (difference > 0) {
				for (int i = 0; i < difference; i++) {
					blurLayers.add(createScrollPane());
				}
			} else if (difference < 0) {
				difference = -difference;
				int idx = blurLayers.size();
				for (int i = 0; i < difference; i++) {
					blurLayers.remove(--idx);
				}
			}
			getChildren().setAll(blurLayers);
		}
		for (int i = 0; i < areas.size(); i++) {
			BlurArea area = areas.get(i);
			ScrollPane pane = blurLayers.get(i);

			weakBind(() -> pane.relocate(area.x.get(), area.y.get()),
					pane, "org.to2mbn.lolixl.ui.blurBackground.locationListener", area.x, area.y);

			weakBind(() -> pane.resize(
					Math.min(area.w.get(), getWidth() - area.x.get()),
					Math.min(area.h.get(), getHeight() - area.y.get())),
					pane, "org.to2mbn.lolixl.ui.blurBackground.sizeListener", area.x, area.y, area.w, area.h, widthProperty(), heightProperty());

			pane.hvalueProperty().bind(area.x.divide(widthProperty().subtract(Bindings.min(area.w, widthProperty().subtract(area.x)))));
			pane.vvalueProperty().bind(area.y.divide(heightProperty().subtract(Bindings.min(area.y, heightProperty().subtract(area.y)))));

			((DoubleProperty) pane.getProperties().get(PROPERTY_OPACITY)).bind(area.opacity);
		}
	}

	private void weakBind(Runnable doBind, Node bindedNode, String propertyName, Observable... dependencies) {
		InvalidationListener listener = new DependenciesInvalidationListener(doBind, dependencies);
		WeakInvalidationListener weakListener = new WeakInvalidationListener(listener);
		Object prev = bindedNode.getProperties().put(propertyName, listener);
		Object prevWeak = bindedNode.getProperties().put(propertyName + ".weak", weakListener);
		if (prev instanceof DependenciesInvalidationListener) {
			for (Observable observable : ((DependenciesInvalidationListener) prev).dependencies) {
				observable.removeListener((InvalidationListener) prevWeak);
			}
		}
		for (Observable dep : dependencies) {
			dep.addListener(weakListener);
		}
		doBind.run();
	}

	private ScrollPane createScrollPane() {
		ScrollPane pane = new ScrollPane();
		pane.getStyleClass().addAll("edge-to-edge", "alpha-scrollpane");
		FXUtils.setSizeToPref(pane);
		pane.setSnapToPixel(false);
		pane.setEffect(new GaussianBlur(gaussianBlurRadius));

		Pane content = new Pane();
		content.backgroundProperty().bind(background);
		FXUtils.bindPrefSize(content, this);
		FXUtils.setSizeToPref(content);
		pane.setContent(content);

		pane.getProperties().put(PROPERTY_OPACITY, content.opacityProperty());
		return pane;
	}

	@Override
	protected void layoutChildren() {}

	static class DependenciesInvalidationListener implements InvalidationListener {

		private Runnable action;
		public final Observable[] dependencies;

		public DependenciesInvalidationListener(Runnable action, Observable[] dependencies) {
			this.action = action;
			this.dependencies = dependencies;
		}

		@Override
		public void invalidated(Observable observable) {
			action.run();
		}
	}

}
