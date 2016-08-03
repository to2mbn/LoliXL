package org.to2mbn.lolixl.ui.impl.container.presenter.panel.tiles;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.view.panel.tils.HiddenTilesView;

@Component(immediate = true)
public class HiddenTilesPresenter extends Presenter<HiddenTilesView> {

	private static final String FXML_LOCATION = "/ui/fxml/panel/hidden_tiles_panel.fxml";

	@Activate
	public void active(ComponentContext compCtx) {
		super.active();
	}

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
	}
}
