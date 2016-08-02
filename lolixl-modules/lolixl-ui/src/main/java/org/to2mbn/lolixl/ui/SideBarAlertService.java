package org.to2mbn.lolixl.ui;

import org.to2mbn.lolixl.ui.component.Tile;

public interface SideBarAlertService {
	/**
	 * 不需要在JavaFX线程下运行
	 */
	void addAlert(Tile alert);

	/**
	 * 不需要在JavaFX线程下运行
	 */
	void removeAlert(Tile alert);
}