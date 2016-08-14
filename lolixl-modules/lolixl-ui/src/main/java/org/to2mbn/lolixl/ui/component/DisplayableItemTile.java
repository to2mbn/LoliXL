package org.to2mbn.lolixl.ui.component;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.image.Image;
import org.to2mbn.lolixl.ui.component.view.DisplayableItemTileView;
import org.to2mbn.lolixl.ui.model.DisplayableItem;
import org.to2mbn.lolixl.utils.BundleUtils;
import org.to2mbn.lolixl.utils.FXUtils;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * 从 {@link DisplayableItem} 创建的磁贴。
 * <p>
 * 该磁贴看起来是这样子的：<br>
 * |[Image] [Text]|<br>
 * 如果说没有Image，那么就会这样：<br>
 * |[Text]|
 * 
 * @author yushijinhun
 */
public class DisplayableItemTile extends Tile {

	private final DisplayableItem item;
	private final DisplayableItemTileView graphic;
	private final Image emptyIcon;

	public DisplayableItemTile(DisplayableItem item) {
		this.item = item;
		try {
			this.emptyIcon = new Image(BundleUtils.getInputStreamFromBundle(getClass(), "/ui/img/no_icon.png"));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		this.graphic = new DisplayableItemTileView();
		this.graphic.textLabel.textProperty().bind(item.getLocalizedName());
		this.graphic.iconView.imageProperty().bind(new ObjectBinding<Image>() {
			ObservableObjectValue<Image> image = item.getIcon();

			{
				bind(image);
			}

			@Override
			protected Image computeValue() {
				return image.get() != null ? image.get() : emptyIcon;
			}
		});
		FXUtils.setButtonGraphic(this, this.graphic);
	}
}
