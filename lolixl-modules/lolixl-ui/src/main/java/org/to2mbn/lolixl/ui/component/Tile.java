package org.to2mbn.lolixl.ui.component;

import javafx.beans.property.ObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;

import java.util.function.Consumer;

public interface Tile {
	void resize(int size);

	String getText();
	void setText(String text);

	ObjectProperty<Image> getIcon();
	void setIcon(Image icon);

	void setOnClicked(Consumer<? super MouseEvent> action);
	Consumer<? super MouseEvent> getOnClicked();
}
