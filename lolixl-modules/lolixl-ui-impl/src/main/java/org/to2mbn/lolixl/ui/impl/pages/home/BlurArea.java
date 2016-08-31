package org.to2mbn.lolixl.ui.impl.pages.home;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Region;

public class BlurArea {

	public DoubleProperty x = new SimpleDoubleProperty();
	public DoubleProperty y = new SimpleDoubleProperty();
	public DoubleProperty w = new SimpleDoubleProperty();
	public DoubleProperty h = new SimpleDoubleProperty();
	public DoubleProperty opacity = new SimpleDoubleProperty();

	public BlurArea() {}

	public BlurArea(Region region) {
		x.bind(region.layoutXProperty().add(region.translateXProperty()));
		y.bind(region.layoutYProperty().add(region.translateYProperty()));
		w.bind(region.widthProperty());
		h.bind(region.heightProperty());
		opacity.bind(region.opacityProperty());
	}

	@Override
	public String toString() {
		return "BlurArea[x=" + x + ", y=" + y + ", w=" + w + ", h=" + h + "]";
	}

}
