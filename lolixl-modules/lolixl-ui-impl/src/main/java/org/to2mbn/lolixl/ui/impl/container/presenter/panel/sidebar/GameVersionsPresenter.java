package org.to2mbn.lolixl.ui.impl.container.presenter.panel.sidebar;

import javafx.scene.control.Label;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.view.panel.sidebar.GameVersionsView;

@Service({ GameVersionsPresenter.class })
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

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
	}
}
