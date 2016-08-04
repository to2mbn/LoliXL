package org.to2mbn.lolixl.ui.impl.container.presenter.panel.settings;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.ui.ConfigurationCategoryViewManager;
import org.to2mbn.lolixl.ui.Panel;
import org.to2mbn.lolixl.ui.PanelDisplayService;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.view.panel.settings.SettingsView;
import org.to2mbn.lolixl.ui.model.SidebarTileElement;
import javafx.beans.value.ObservableStringValue;

@Service({ SidebarTileElement.class })
@Component(immediate = true)
public class SettingsPresenter extends Presenter<SettingsView> implements SidebarTileElement {

	private static final String FXML_LOCATION = "/ui/fxml/panel/settings_panel.fxml";

	@Reference
	private PanelDisplayService displayService;

	@Reference
	private ConfigurationCategoryViewManager categoryManager;

	@Activate
	public void active(ComponentContext compCtx) {
		super.active();
	}

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
	}

	@Override
	protected void initializePresenter() {
		view.categoryContainer.setItems(categoryManager.getProviders());

		view.categoryContainer.selectionModelProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue != null) {
				view.contentContainer.getChildren().clear();
			}
			view.contentContainer.getChildren().add(newValue.getSelectedItem().createConfiguringPanel());
		});
	}

	@Override
	public ObservableStringValue getLocalizedName() {
		return I18N.localize("org.to2mbn.lolixl.ui.impl.container.tiles.management.title");
	}

	@Override
	public Tile createTile() {
		Tile tile = SidebarTileElement.super.createTile();

		Panel panel = displayService.newPanel();
		panel.bindButton(tile);
		panel.bindItem(this);

		panel.contentProperty().set(view.rootContainer);

		return tile;
	}

}
