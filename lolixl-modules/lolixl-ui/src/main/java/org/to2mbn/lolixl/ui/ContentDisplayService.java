package org.to2mbn.lolixl.ui;

import javafx.scene.layout.Pane;

import java.util.List;

public interface ContentDisplayService {
	void displayContent(Pane pane, boolean hideSidebar);

	default void displayContent(Pane pane) {
		displayContent(pane, false);
	}

	/**
	 * 关闭当前置于最顶层的Content
	 *
	 * @return 当当前已为最底层Content时会返回<code>false</code>, 否则返回<code>true</code>
	 */
	boolean closeCurrentContent();

	List<Pane> getAvailableContents();
}
