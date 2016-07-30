package org.to2mbn.lolixl.ui.impl.container.presenter.panel;

import static java.util.stream.Collectors.*;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.to2mbn.lolixl.core.config.ConfigurationEvent;
import org.to2mbn.lolixl.ui.SideBarTileService;
import org.to2mbn.lolixl.ui.SideBarTileService.StackingStatus;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.component.TileListCell;
import org.to2mbn.lolixl.ui.container.panelcontent.PanelContentPresenter;
import org.to2mbn.lolixl.ui.impl.container.view.panel.TileManagingPanelContentView;
import org.to2mbn.lolixl.ui.model.SidebarTileElement;

@Service({ EventHandler.class })
@Properties({
		@Property(name = EventConstants.EVENT_TOPIC, value = ConfigurationEvent.TOPIC_CONFIGURATION),
		@Property(name = EventConstants.EVENT_FILTER, value = "(" + ConfigurationEvent.KEY_CATEGORY + "=" + SideBarTileService.CATEGORY_SIDEBAR_TILES + ")")
})
@Component(immediate = true)
public class TileManagingPanelContentPresenter extends PanelContentPresenter<TileManagingPanelContentView> implements EventHandler {

	private static final String FXML_LOCATION = "/ui/fxml/panel/tile_managing_panel.fxml";
	
	private static final Logger LOGGER = Logger.getLogger(TileManagingPanelContentPresenter.class.getCanonicalName());

	@Reference
	private SideBarTileService tileService;

	private ObservableList<Tile> tiles;

	@Override
	public void handleEvent(Event event) {
		LOGGER.fine(() -> "Event-driven tiles refreshing: " + event);
		Platform.runLater(() -> refreshTiles());
	}

	@Override
	public void postInitialize() {
		view.listView.setCellFactory(view -> new TileListCell());
		view.upButton.setOnAction(this::onUpButtonClicked);
		view.downButton.setOnAction(this::onDownButtonClicked);

		tiles = FXCollections.observableArrayList();
	}

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
	}

	@Override
	public void onPanelShown() {
		refreshTiles();
	}

	private void refreshTiles() {
		tiles.setAll(tileService.getTiles(StackingStatus.SHOWN, StackingStatus.HIDDEN).stream()
				.map(tileService::getTileComponent)
				.collect(toList()));
	}

	private void onUpButtonClicked(ActionEvent event) {
		Tile selectedTile = view.listView.getSelectionModel().getSelectedItem();
		moveTile(selectedTile, -1);
	}

	private void onDownButtonClicked(ActionEvent event) {
		Tile selectedTile = view.listView.getSelectionModel().getSelectedItem();
		moveTile(selectedTile, 1);
	}

	private void moveTile(Tile tile, int offset) {
		SidebarTileElement entry = tileService.getTileByComponent(tile);
		if (entry != null) {
			tileService.moveTile(entry, offset);
		}
	}
}
