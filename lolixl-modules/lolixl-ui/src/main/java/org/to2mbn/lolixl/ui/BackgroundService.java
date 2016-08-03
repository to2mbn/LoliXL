package org.to2mbn.lolixl.ui;

import javafx.scene.layout.Background;

public interface BackgroundService {
	
	/**
	 * 需要在JavaFX线程下运行
	 * 
	 * @param background
	 */
	void setBackground(Background background);

	Background getBackground();
}
