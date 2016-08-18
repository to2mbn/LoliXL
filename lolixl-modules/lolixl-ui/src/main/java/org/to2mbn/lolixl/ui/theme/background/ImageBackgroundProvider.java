package org.to2mbn.lolixl.ui.theme.background;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;

public abstract class ImageBackgroundProvider implements BackgroundProvider {

	private volatile ObservableObjectValue<Background> background;
	private Object lock = new Object();

	@Override
	public ObservableObjectValue<Background> getBackground() {
		if (background == null) {
			synchronized (lock) {
				if (background == null) {
					ObservableObjectValue<Image> bgimg = getBackgroundImage();
					background = Bindings.createObjectBinding(() -> new Background(new BackgroundImage(
							bgimg.get(),
							BackgroundRepeat.NO_REPEAT,
							BackgroundRepeat.NO_REPEAT,
							BackgroundPosition.CENTER,
							new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true))), bgimg);
				}
			}
		}
		return background;
	}

	public abstract ObservableObjectValue<Image> getBackgroundImage();

}
