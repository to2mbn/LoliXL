package org.to2mbn.lolixl.ui.impl.container.presenter.panel.sidebar;

import javafx.scene.control.Label;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.view.panel.sidebar.GameVersionsView;

public class GameVersionsPresenter extends Presenter<GameVersionsView> {
	// TODO
	public static class GameVersionCategory {
		private String alias;
		private String mcDirPath;

		private Label toLabel() {
			return null;
		}
	}

	private static final String LOCATION_OF_FXML = "/ui/fxml/panel/game_versions_panel.fxml";

	@Override
	protected String getFxmlLocation() {
		return LOCATION_OF_FXML;
	}
}
