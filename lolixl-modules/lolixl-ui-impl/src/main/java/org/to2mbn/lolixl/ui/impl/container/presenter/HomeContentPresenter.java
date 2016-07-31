package org.to2mbn.lolixl.ui.impl.container.presenter;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.to2mbn.lolixl.ui.Panel;
import org.to2mbn.lolixl.ui.PanelDisplayService;
import org.to2mbn.lolixl.ui.SideBarTileService;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panel.SettingsPanelContentPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panel.TileManagingPanelContentPresenter;
import org.to2mbn.lolixl.ui.impl.container.view.HomeContentView;
import java.util.function.Supplier;

@Component
public class HomeContentPresenter extends Presenter<HomeContentView> {
	private static final String LOCATION_OF_FXML = "/ui/fxml/container/home_content.fxml";

	@Reference
	private SideBarTileService tileService;

	@Reference
	private PanelDisplayService panelDisplayService;

	private SettingsPanelContentPresenter settingsPanelContentPresenter;

	private TileManagingPanelContentPresenter tileManagingPanelContentPresenter;

	private Tile settingsTile = getValue(() -> {
		Panel panel = panelDisplayService.newPanel();
		panel.setTitle("papapa"); // TODO
		panel.setIcon(null); // TODO
		panel.setContent(settingsPanelContentPresenter.getView().rootContainer);
		panel.setShowOperation(settingsPanelContentPresenter::onShown);
		Tile tile = new Tile();
		tile.setOnAction(event -> panel.show());
		return tile;
	});

	private Tile tileManagementTile = getValue(() -> {
		Panel panel = panelDisplayService.newPanel();
		panel.setTitle("lalala"); // TODO
		panel.setIcon(null); // TODO
		panel.setContent(tileManagingPanelContentPresenter.getView().rootContainer);
		panel.setShowOperation(tileManagingPanelContentPresenter::onShown);
		Tile tile = new Tile();
		tile.setOnAction(event -> panel.show());
		return tile;
	});

	@Override
	public void postInitialize() {
		view.tileContainer.getChildren().add(settingsTile);
		view.tileRootContainer.setBottom(tileManagementTile);
	}

	@Override
	protected String getFxmlLocation() {
		return LOCATION_OF_FXML;
	}

	public void setSettingsPanelContentPresenter(SettingsPanelContentPresenter presenter) {
		settingsPanelContentPresenter = presenter;
	}

	public void setTileManagingPanelContentPresenter(TileManagingPanelContentPresenter presenter) {
		tileManagingPanelContentPresenter = presenter;
	}

	private <T> T getValue(Supplier<T> supplier) {
		return supplier.get();
	}
}