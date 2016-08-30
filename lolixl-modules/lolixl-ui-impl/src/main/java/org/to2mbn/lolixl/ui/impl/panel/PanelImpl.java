package org.to2mbn.lolixl.ui.impl.panel;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import org.to2mbn.lolixl.ui.panel.Panel;
import org.to2mbn.lolixl.utils.FXUtils;
import java.util.function.Consumer;

public class PanelImpl implements Panel {
	private final Consumer<Panel> internalOnShow;
	private final Consumer<Panel> internalOnClose;

	private final ObjectProperty<Image> iconProperty;
	private final StringProperty titleProperty;
	private final ObjectProperty<Runnable> onShownProperty, onClosedProperty;
	private final ObjectProperty<Region> contentProperty;

	public PanelImpl(Consumer<Panel> _onShow, Consumer<Panel> _onClose) {
		internalOnShow = _onShow;
		internalOnClose = _onClose;
		iconProperty = new SimpleObjectProperty<>(null);
		titleProperty = new SimpleStringProperty("");
		onShownProperty = new SimpleObjectProperty<>(() -> {});
		onClosedProperty = new SimpleObjectProperty<>(() -> {});
		contentProperty = new SimpleObjectProperty<>(null);
	}

	@Override
	public ObjectProperty<Image> iconProperty() {
		return iconProperty;
	}

	@Override
	public StringProperty titleProperty() {
		return titleProperty;
	}

	@Override
	public ObjectProperty<Runnable> onShownProperty() {
		return onShownProperty;
	}

	@Override
	public ObjectProperty<Runnable> onClosedProperty() {
		return onClosedProperty;
	}

	@Override
	public ObjectProperty<Region> contentProperty() {
		return contentProperty;
	}

	@Override
	public void show() {
		FXUtils.checkFxThread();
		onShownProperty.get().run();
		internalOnShow.accept(this);
	}

	@Override
	public void hide() {
		FXUtils.checkFxThread();
		onClosedProperty.get().run();
		internalOnClose.accept(this);
	}
}
