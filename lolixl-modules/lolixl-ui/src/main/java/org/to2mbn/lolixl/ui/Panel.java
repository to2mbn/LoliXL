package org.to2mbn.lolixl.ui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;

public interface Panel {

	/*
	 * 图标为null则不显示图标
	 * 设置图标/标题的时候使用渐变特效
	 * CloseOperation的默认动作是关闭当前面板，即this.hide()
	 * 打开/关闭 时使用 滑入/滑出 特效
	 */

	ObjectProperty<Image> iconProperty();

	StringProperty titleProperty();

	ObjectProperty<Runnable> onShownProperty();

	ObjectProperty<Runnable> onClosedProperty();

	ObjectProperty<Region> contentProperty();

	/**
	 * 需要在JavaFX线程运行
	 */
	void show();
	/**
	 * 需要在JavaFX线程运行
	 */
	void hide();
}
