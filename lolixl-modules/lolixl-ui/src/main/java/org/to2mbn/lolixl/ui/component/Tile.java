package org.to2mbn.lolixl.ui.component;

import javafx.scene.control.Button;

public class Tile extends Button {
	private final String nameTag;

	public Tile(String _nameTag) {
		nameTag = _nameTag;
	}

	/**
	 * 在持续化磁贴顺序中用于标识的Tag
	 * 命名格式: a-example-tag
	 *
	 * @return Tag
	 */
	public String getNameTag() {
		return nameTag;
	}
}
