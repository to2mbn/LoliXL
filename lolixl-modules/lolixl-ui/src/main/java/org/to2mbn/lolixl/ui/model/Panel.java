package org.to2mbn.lolixl.ui.model;

import javax.swing.Icon;
import javafx.scene.layout.Pane;

public interface Panel {

	/*
	 * 图标为null则不显示图标
	 * 设置图标/标题的时候使用渐变特效
	 * CloseOperation的默认动作是关闭当前面板，即this.close()
	 * 打开/关闭 时使用 滑入/滑出 特效
	 */

	Icon getIcon();
	void setIcon(Icon icon);

	String getTitle();
	void setTitle(String title);

	Runnable getCloseOperation();
	/**
	 * 设置将关闭面板时的动作。
	 * 
	 * @param onClose 将关闭面板时的动作，null代表使用默认动作
	 */
	void setCloseOperation(Runnable onClose);

	Pane getContent();
	void setContent(Pane content);

	void open();
	void close();
}
