package org.to2mbn.lolixl.ui.impl.container.presenter.panel.sidebar;

import javafx.scene.control.Label;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.view.panel.sidebar.GameVersionsView;

@Component(immediate = true)
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

	@Activate
	public void active(ComponentContext compCtx) {
		super.active();
	}

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
	}
}
