package org.to2mbn.lolixl.ui.impl.container.presenter.panel.sidebar;

import javafx.scene.control.Label;
import org.osgi.framework.BundleContext;
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

	private static final String FXML_LOCATION = "/ui/fxml/panel/game_versions_panel.fxml";

	public GameVersionsPresenter(BundleContext ctx) {
		super(ctx);
	}

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
	}
}
