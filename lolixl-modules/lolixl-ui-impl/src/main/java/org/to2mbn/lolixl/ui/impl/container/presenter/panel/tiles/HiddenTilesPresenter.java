package org.to2mbn.lolixl.ui.impl.container.presenter.panel.tiles;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.view.panel.tils.HiddenTilesView;

@Service({ HiddenTilesPresenter.class })
@Component(immediate = true)
public class HiddenTilesPresenter extends Presenter<HiddenTilesView> {

	private static final String FXML_LOCATION = "/ui/fxml/panel/hidden_tiles_panel.fxml";

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
	}
}
