package org.to2mbn.lolixl.ui.impl.container.presenter.panelcontent;

import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.view.panelcontent.TileManagingPanelContentView;

import java.io.IOException;

public class TileManagingPanelContentPresenter extends Presenter<TileManagingPanelContentView> {
	private static final String LOCATION_OF_FXML = "/ui/fxml/panel/tile_managing_panel.fxml";

	public void initialize() throws IOException {
		super.initialize(LOCATION_OF_FXML);
	}
}
