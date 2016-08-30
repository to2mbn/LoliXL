package org.to2mbn.lolixl.ui.impl.pages.tile;

import java.util.ArrayList;
import java.util.List;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.ui.DisplayableItem;
import org.to2mbn.lolixl.ui.DisplayableTile;
import org.to2mbn.lolixl.ui.Presenter;
import org.to2mbn.lolixl.ui.component.ItemTile;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.impl.pages.home.HomeContentPresenter;
import org.to2mbn.lolixl.ui.panel.Panel;
import org.to2mbn.lolixl.ui.panel.PanelDisplayService;
import org.to2mbn.lolixl.ui.sidebar.SidebarTileService;
import org.to2mbn.lolixl.ui.sidebar.SidebarTileElement;
import org.to2mbn.lolixl.ui.sidebar.SidebarTileService.StackingStatus;
import org.to2mbn.lolixl.utils.MappedObservableList;
import org.to2mbn.lolixl.utils.binding.FxConstants;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ListBinding;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;

@Component(immediate = true)
public class TileManagementPresenter extends Presenter<TileManagementView> implements DisplayableTile {

	private static final String FXML_LOCATION = "fxml/org.to2mbn.lolixl.ui.tiles_management/content.fxml";

	@Reference
	private HomeContentPresenter homeContentPresenter;

	@Reference
	private PanelDisplayService panelDisplayService;

	@Reference
	private SidebarTileService tileService;

	private MappedObservableList<SidebarTileElement, Tile> tilesMapping;
	private ObservableList<Node> tileContainerElements;

	@Activate
	public void active(ComponentContext compCtx) {
		super.active();
	}

	@Override
	protected void initializePresenter() {
		tilesMapping = new MappedObservableList<>(tileService.getTiles(StackingStatus.SHOWN, StackingStatus.HIDDEN), this::createTile);
		tileContainerElements = new ListBinding<Node>() {

			ObservableIntegerValue maxShownTiles = tileService.maxShownTilesProperty();

			{
				bind(tilesMapping, maxShownTiles);
			}

			@Override
			protected ObservableList<Node> computeValue() {
				List<Node> elements = new ArrayList<>();
				int maxShown = maxShownTiles.get();
				if (maxShown > 0 && elements.size() > 0) {
					elements.add(view.shownTilesLabel);
				}
				int i = 0;
				for (Tile tile : tilesMapping) {
					if (i == maxShown) {
						elements.add(view.hiddenTileLabel);
					}
					elements.add(tile);
					i++;
				}
				return FXCollections.observableList(elements);
			}
		};
		Bindings.bindContent(view.tilesContainer.getChildren(), tileContainerElements);

		bindManagementTile();
	}

	private Tile createTile(SidebarTileElement element) {
		Tile tile = element.createTile();
		tile.getStyleClass().add("xl-tile-management-tile-element");
		return tile;
	}

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
	}

	@Override
	public ObservableStringValue getLocalizedName() {
		return I18N.localize("org.to2mbn.lolixl.ui.impl.tiles.management.title");
	}

	private void bindManagementTile() {
		Tile tile = new ItemTile(new DisplayableItem() {

			@Override
			public ObservableStringValue getLocalizedName() {
				return FxConstants.string("...");
			}
		});
		Panel panel = panelDisplayService.newPanel();
		panel.bindButton(tile);
		panel.bindItem(this);
		panel.contentProperty().set(view.rootContainer);
		homeContentPresenter.setManagementTile(tile);
	}

}
