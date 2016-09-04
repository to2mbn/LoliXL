package org.to2mbn.lolixl.ui.impl.pages.home;

import org.to2mbn.lolixl.utils.ObservableContext;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Region;

public class BlurArea {

	public Region node;
	public DoubleProperty x = new SimpleDoubleProperty();
	public DoubleProperty y = new SimpleDoubleProperty();
	public DoubleProperty w = new SimpleDoubleProperty();
	public DoubleProperty h = new SimpleDoubleProperty();
	public DoubleProperty opacity = new SimpleDoubleProperty();
	public ObservableContext absPosChangeNotfier = new ObservableContext();

	public BlurArea() {}

	public BlurArea(Region region) {
		this.node = region;
		x.bind(region.translateXProperty());
		y.bind(region.translateYProperty());
		w.bind(region.widthProperty());
		h.bind(region.heightProperty());
		opacity.bind(region.opacityProperty());
		absPosChangeNotfier.bind(node.layoutXProperty(), node.layoutYProperty());
	}

	@Override
	public String toString() {
		return "BlurArea[node=" + node + ", x=" + x + ", y=" + y + ", w=" + w + ", h=" + h + ", opacity=" + opacity + "]";
	}

}
