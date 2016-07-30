package org.to2mbn.lolixl.ui.impl.container.presenter;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.to2mbn.lolixl.ui.SideBarTileService;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.view.HomeContentView;

@Component
public class HomeContentPresenter extends Presenter<HomeContentView> {
	private static final String LOCATION_OF_FXML = "/ui/fxml/container/home_content.fxml";

	@Reference
	private SideBarTileService tileService;

	private int tileSize = 60;

	@Override
	public void postInitialize() {
	}

	@Override
	protected String getFxmlLocation() {
		return LOCATION_OF_FXML;
	}

}