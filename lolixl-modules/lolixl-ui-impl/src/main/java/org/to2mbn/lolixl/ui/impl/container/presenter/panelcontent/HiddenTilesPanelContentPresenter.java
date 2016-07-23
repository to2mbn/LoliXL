package org.to2mbn.lolixl.ui.impl.container.presenter.panelcontent;

import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.view.panelcontent.HiddenTilesPanelContentView;

public class HiddenTilesPanelContentPresenter extends Presenter<HiddenTilesPanelContentView> {
	private static final String LOCATION_OF_FXML = "/ui/fxml/panel/hidden_tiles_panel.fxml";

	@Override
	protected String getFxmlLocation() {
		return LOCATION_OF_FXML;
	}
}
