package org.to2mbn.lolixl.ui.impl.pages.home;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.to2mbn.lolixl.utils.FXUtils;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class BlurBackgroundPane extends StackPane {

	private static final String PROPERTY_CHILD = "org.to2mbn.lolixl.ui.blurBackground.clipPanel.child";

	private double gaussianBlurRadius = 10.0;

	private Supplier<List<BlurArea>> blurArea;
	private ObservableValue<Background> background;
	private List<Pane> blurLayers = new ArrayList<>();

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
					blurLayers.add(createClipPane());
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
			Pane pane = blurLayers.get(i);
			Pane child = (Pane) pane.getProperties().get(PROPERTY_CHILD);
			Rectangle clip = new Rectangle();
			clip.widthProperty().bind(area.w);
			clip.heightProperty().bind(area.h);
			pane.setClip(clip);

			weakBind(() -> {
				Point2D pos = localAreaPosition(area);
				pane.relocate(pos.getX(), pos.getY());
				child.relocate(-pos.getX(), -pos.getY());
			}, pane, "org.to2mbn.lolixl.ui.blurBackground.locationListener", area.x, area.y);

			weakBind(() -> {
				Point2D pos = localAreaPosition(area);
				pane.resize(
						Math.min(area.w.get(), getWidth() - pos.getX()),
						Math.min(area.h.get(), getHeight() - pos.getY()));
			}, pane, "org.to2mbn.lolixl.ui.blurBackground.sizeListener", area.x, area.y, area.w, area.h, widthProperty(), heightProperty());

			child.opacityProperty().bind(area.opacity);
		}
	}

	private Point2D localAreaPosition(BlurArea area) {
		Point2D p0 = localToScene(0, 0);
		Point2D p1 = area.node.localToScene(0, 0);
		return p1.subtract(p0);
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

	private Pane createClipPane() {
		Pane backgroundPane = new Pane();
		backgroundPane.backgroundProperty().bind(background);
		FXUtils.bindPrefSize(backgroundPane, this);
		FXUtils.setSizeToPref(backgroundPane);
		backgroundPane.setManaged(false);
		backgroundPane.setEffect(new GaussianBlur(gaussianBlurRadius));
		weakBind(() -> backgroundPane.resize(getWidth(), getHeight()),
				backgroundPane, "org.to2mbn.lolixl.ui.blurBackground.backgroundSizeListener", this.widthProperty(), this.heightProperty());

		Pane clipPane = new Pane();
		clipPane.getChildren().add(backgroundPane);
		FXUtils.setSizeToPref(clipPane);
		clipPane.setStyle("-fx-background-color: transparent;");

		clipPane.getProperties().put(PROPERTY_CHILD, backgroundPane);
		return clipPane;
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
