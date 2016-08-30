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
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class BlurBackgroundPane extends StackPane {

	private static final String PROPERTY_OPACITY = "org.to2mbn.lolixl.ui.blurBackground.opacityProperty";

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
		InvalidationListener listener = dummy -> doBind.run();
		WeakInvalidationListener weakListener = new WeakInvalidationListener(listener);
		bindedNode.getProperties().put(propertyName, listener);
		String keyWeak = propertyName + ".weak";
		Object prev = bindedNode.getProperties().put(keyWeak, weakListener);
		if (prev instanceof WeakInvalidationListener) {
			for (Observable dep : dependencies) {
				dep.removeListener((InvalidationListener) prev);
			}
		}
		for (Observable dep : dependencies) {
			dep.addListener(weakListener);
		}
		doBind.run();
	}

	private ScrollPane createScrollPane() {
		ScrollPane pane = new ScrollPane();
		pane.setHbarPolicy(ScrollBarPolicy.NEVER);
		pane.setVbarPolicy(ScrollBarPolicy.NEVER);
		pane.setBackground(null);
		pane.setStyle("-fx-control-inner-background: transparent;");
		pane.getStyleClass().add("edge-to-edge");
		FXUtils.setSizeToPref(pane);
		pane.setEffect(new BoxBlur());

		Pane content = new Pane();
		content.backgroundProperty().set(null);
		content.backgroundProperty().bind(background);
		FXUtils.bindPrefSize(content, this);
		FXUtils.setSizeToPref(content);
		pane.setContent(content);

		pane.getProperties().put(PROPERTY_OPACITY, content.opacityProperty());
		return pane;
	}

	@Override
	protected void layoutChildren() {}

}
