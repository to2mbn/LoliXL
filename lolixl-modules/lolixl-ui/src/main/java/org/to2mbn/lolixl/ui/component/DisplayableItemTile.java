package org.to2mbn.lolixl.ui.component;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import org.to2mbn.lolixl.ui.component.view.DisplayableItemTileView;
import org.to2mbn.lolixl.ui.model.DisplayableItem;
import org.to2mbn.lolixl.utils.FXUtils;

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
		this.emptyIcon = new Image("/ui/img/no_icon.png");
		setContentDisplay(ContentDisplay.TOP);

		graphic = new DisplayableItemTileView();
		graphic.textLabel.textProperty().bind(item.getLocalizedName());
		graphic.iconView.imageProperty().bind(new ObjectBinding<Image>() {
			ObservableObjectValue<Image> image = item.getIcon();

			{
				bind(image);
			}

			@Override
			protected Image computeValue() {
				return image.get() != null ? image.get() : emptyIcon;
			}
		});
		FXUtils.setButtonGraphic(this, graphic);
	}
}
