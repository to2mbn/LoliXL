package org.to2mbn.lolixl.ui.component;

import javafx.scene.control.Button;

public class Tile extends Button {
	/**
	 * 修改磁贴的大小
	 * 对于磁贴 其宽高必须相等
	 *
	 * @param width
	 * @param height
	 * @throws IllegalArgumentException 如果给定的width和height不等
	 */
	@Override
	public void resize(double width, double height) {
		if (width != height) {
			throw new IllegalArgumentException("width must equal height");
		}
		super.resize(width, height);
	}
}
