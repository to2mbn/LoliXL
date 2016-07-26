package org.to2mbn.lolixl.ui.impl.container.presenter.panelcontent;

import org.apache.felix.scr.annotations.Component;
import org.to2mbn.lolixl.ui.SettingsCategoriesManagingService;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.view.panelcontent.SettingsContentPanelView;

@Component
public class SettingsPanelContentPresenter extends Presenter<SettingsContentPanelView> implements SettingsCategoriesManagingService {
	private static final String LOCATION_OF_FXML = "/ui/fxml/panel/settings_panel.fxml";

	@Override
	protected String getFxmlLocation() {
		return LOCATION_OF_FXML;
	}
}
