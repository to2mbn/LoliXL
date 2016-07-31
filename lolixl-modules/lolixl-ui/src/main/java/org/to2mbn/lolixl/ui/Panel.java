package org.to2mbn.lolixl.ui;

import javafx.scene.Parent;
import javafx.scene.image.Image;

public interface Panel {

	/*
	 * 图标为null则不显示图标
	 * 设置图标/标题的时候使用渐变特效
	 * CloseOperation的默认动作是关闭当前面板，即this.hide()
	 * 打开/关闭 时使用 滑入/滑出 特效
	 */

	Image getIcon();
	void setIcon(Image icon);

	String getTitle();
	void setTitle(String title);

	Runnable getShowOperation();
	void setShowOperation(Runnable onShow);

	Runnable getHideOperation();
	/**
	 * 设置将关闭面板时的动作。
	 *
	 * @param onHide 将关闭面板时的动作，null代表使用默认动作
	 */
	void setHideOperation(Runnable onHide);

	Parent getContent();
	void setContent(Parent content);

	/**
	 * 需要在JavaFX线程运行
	 */
	void show();
	/**
	 * 需要在JavaFX线程运行
	 */
	void hide();
}
