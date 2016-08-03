package org.to2mbn.lolixl.ui.impl.container.presenter.panel.tiles;

import static java.util.stream.Collectors.toList;
import javafx.application.Platform;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.to2mbn.lolixl.core.config.ConfigurationEvent;
import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.ui.Panel;
import org.to2mbn.lolixl.ui.PanelDisplayService;
import org.to2mbn.lolixl.ui.SideBarTileService;
import org.to2mbn.lolixl.ui.SideBarTileService.StackingStatus;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.HomeContentPresenter;
import org.to2mbn.lolixl.ui.impl.container.view.panel.tils.TileManagingView;
import org.to2mbn.lolixl.ui.model.DisplayableTile;
import org.to2mbn.lolixl.ui.model.SidebarTileElement;
import java.util.logging.Logger;

@Service({ EventHandler.class })
@Properties({
		@Property(name = EventConstants.EVENT_TOPIC, value = ConfigurationEvent.TOPIC_CONFIGURATION),
		@Property(name = EventConstants.EVENT_FILTER, value = "(" + ConfigurationEvent.KEY_CATEGORY + "=" + SideBarTileService.CATEGORY_SIDEBAR_TILES + ")")
})
@Component(immediate = true)
public class TileManagingPresenter extends Presenter<TileManagingView> implements EventHandler, DisplayableTile {

	private static final String FXML_LOCATION = "/ui/fxml/panel/tile_managing_panel.fxml";

	private static final Logger LOGGER = Logger.getLogger(TileManagingPresenter.class.getCanonicalName());

	@Reference
	private HomeContentPresenter homeContentPresenter;

	@Reference
	private PanelDisplayService panelDisplayService;

	@Reference
	private SideBarTileService tileService;

	private ObservableList<Tile> tiles;

	@Activate
	public void active(ComponentContext compCtx) {
		super.active();
	}

	@Override
	public void handleEvent(Event event) {
		LOGGER.fine(() -> "Event-driven tiles refreshing: " + event);
		Platform.runLater(this::refreshTiles);
	}

	@Override
	protected void initializePresenter() {
		view.upButton.setOnAction(this::onUpButtonClicked);
		view.downButton.setOnAction(this::onDownButtonClicked);
		tiles = FXCollections.observableArrayList();
		bindManagementTile();
	}

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
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

	@Override
	public ObservableStringValue getLocalizedName() {
		return I18N.localize("org.to2mbn.lolixl.ui.impl.container.tiles.management.title");
	}

	private void bindManagementTile() {
		Tile tile = DisplayableTile.super.createTile();
		Panel panel = panelDisplayService.newPanel();
		panel.bindButton(tile);
		panel.bindItem(this);
		panel.onShownProperty().set(this::refreshTiles);
		homeContentPresenter.setManagementTile(tile);
	}

}
