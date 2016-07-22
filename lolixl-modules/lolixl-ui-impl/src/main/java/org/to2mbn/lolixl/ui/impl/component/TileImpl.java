package org.to2mbn.lolixl.ui.impl.component;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import org.to2mbn.lolixl.ui.component.Tile;

import java.util.Objects;
import java.util.function.Consumer;

public class TileImpl extends Button implements Tile {
	private final ObjectProperty<Image> icon = new SimpleObjectProperty<>();
	private Consumer<? super MouseEvent> onClicked;

	@Override
	public void resize(int size) {
		resize(size, size);
	}

	@Override
	public ObjectProperty<Image> getIcon() {
		return icon;
	}

	@Override
	public void setIcon(Image _icon) {
		icon.set(Objects.requireNonNull(_icon));
	}

	@Override
	public void setOnClicked(Consumer<? super MouseEvent> action) {
		onClicked = Objects.requireNonNull(action);
		setOnMouseClicked(event -> onClicked.accept(event));
	}

	@Override
	public Consumer<? super MouseEvent> getOnClicked() {
		return onClicked;
	}

	// setText和getText方法被Button类实现
}
