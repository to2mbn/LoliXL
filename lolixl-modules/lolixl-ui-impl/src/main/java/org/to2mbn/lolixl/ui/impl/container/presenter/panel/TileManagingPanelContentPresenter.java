package org.to2mbn.lolixl.ui.impl.container.presenter.panel;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.layout.Region;
import org.to2mbn.lolixl.core.config.ConfigurationCategory;
import org.to2mbn.lolixl.ui.SideBarTileService;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.component.TileListCell;
import org.to2mbn.lolixl.ui.container.panelcontent.PanelContentPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.HomeContentPresenter;
import org.to2mbn.lolixl.ui.impl.container.view.panel.TileManagingPanelContentView;
import org.to2mbn.lolixl.utils.ObservableContext;

import java.util.List;
import java.util.stream.Stream;

public class TileManagingPanelContentPresenter extends PanelContentPresenter<TileManagingPanelContentView> implements ConfigurationCategory<TilesOrderConfiguration> {
	private static final String LOCATION_OF_FXML = "/ui/fxml/panel/tile_managing_panel.fxml";

	private final TilesOrderConfiguration configuration = new TilesOrderConfiguration();
	private HomeContentPresenter homeContentPresenter;

	public void setHomeContentPresenter(HomeContentPresenter _homeContentPresenter) {
		homeContentPresenter = _homeContentPresenter;
	}

	@Override
	public void postInitialize() {
		view.listView.setCellFactory(view -> new TileListCell());
		view.upButton.setOnAction(this::onUpButtonClicked);
		view.downButton.setOnAction(this::onDownButtonClicked);

		// 根据configuration读取磁贴顺序并设置生效
		Tile[] tiles = new Tile[configuration.tilesOrder.size()];
		Stream<Tile> oldTiles = Stream.of(homeContentPresenter.getTiles(SideBarTileService.StackingStatus.COMMON));
		configuration.tilesOrder.forEach((tag, index) -> tiles[index] = oldTiles.filter(it -> it.getNameTag().equals(tag)).findFirst().get());
		homeContentPresenter.updateTilesOrder(tiles);
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

	@Override
	public Region createConfiguringPanel() {
		return null; // 不需要
	}

	@Override
	public String getLocalizedName() {
		return null; // 不需要
	}

	@Override
	public void setObservableContext(ObservableContext ctx) {
	}

	@Override
	public TilesOrderConfiguration store() {
		return configuration;
	}

	@Override
	public void restore(TilesOrderConfiguration memento) {
		configuration.tiles.clear();
		configuration.tiles.putAll(memento.tiles);
	}

	@Override
	public Class<? extends TilesOrderConfiguration> getMementoType() {
		return configuration.getClass();
	}

	private void updateListData() {
		view.listView.setItems(FXCollections.observableArrayList(homeContentPresenter.getTiles(SideBarTileService.StackingStatus.COMMON)));
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
