package org.to2mbn.lolixl.ui.impl.container.presenter.panelcontent;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import org.to2mbn.lolixl.ui.TileManagingService;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.container.panelcontent.PanelContentPresenter;
import org.to2mbn.lolixl.ui.impl.component.TileListCell;
import org.to2mbn.lolixl.ui.impl.container.presenter.content.HomeContentPresenter;
import org.to2mbn.lolixl.ui.impl.container.view.panelcontent.TileManagingPanelContentView;

import java.util.List;

public class TileManagingPanelContentPresenter extends PanelContentPresenter<TileManagingPanelContentView> {
	private static final String LOCATION_OF_FXML = "/ui/fxml/panel/tile_managing_panel.fxml";

	private HomeContentPresenter homeContentPresenter;

	public void setHomeContentPresenter(HomeContentPresenter _homeContentPresenter) {
		homeContentPresenter = _homeContentPresenter;
	}

	@Override
	public void postInitialize() {
		view.listView.setCellFactory(view -> new TileListCell());
		view.upButton.setOnAction(this::onUpButtonClicked);
		view.downButton.setOnAction(this::onDownButtonClicked);
		updateListData();
	}

	@Override
	protected String getFxmlLocation() {
		return LOCATION_OF_FXML;
	}

	@Override
	public void onPanelShown() {
		updateListData();
	}

	private void updateListData() {
		view.listView.setItems(FXCollections.observableArrayList(homeContentPresenter.getTiles(TileManagingService.TileStatus.COMMON)));
	}

	private void onUpButtonClicked(ActionEvent event) {
		Tile selectedTile = (Tile) view.listView.getSelectionModel().getSelectedItem();
		updateTileOrder(true, selectedTile);
	}

	private void onDownButtonClicked(ActionEvent event) {
		Tile selectedTile = (Tile) view.listView.getSelectionModel().getSelectedItem();
		updateTileOrder(false, selectedTile);
	}

	private void updateTileOrder(boolean isUp, Tile tile) {
		List<Tile> tiles = view.listView.getItems();
		int index = tiles.indexOf(tile);
		if ((index == 0 && isUp) || (index == tiles.size() - 1 && !isUp)) {
			return;
		}
		int newIndex = index + (isUp ? -1 : 1);
		Tile anotherTile = tiles.get(newIndex);
		tiles.set(newIndex, tile);
		tiles.set(newIndex + (isUp ? 1 : -1), anotherTile);
		homeContentPresenter.updateTilesOrder(tiles.toArray(new Tile[tiles.size()]));
		updateListData();
	}
}
