package org.to2mbn.lolixl.ui.impl.container.presenter;

import javafx.scene.image.ImageView;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.ui.Panel;
import org.to2mbn.lolixl.ui.PanelDisplayService;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panel.tiles.TileManagingPresenter;
import org.to2mbn.lolixl.ui.impl.container.view.HomeContentView;
import java.util.function.Supplier;

@Service({ HomeContentPresenter.class })
@Component(immediate = true)
public class HomeContentPresenter extends Presenter<HomeContentView> {

	private static final String FXML_LOCATION = "/ui/fxml/container/home_content.fxml";

	@Reference
	private TileManagingPresenter tileManagingPresenter;

	@Reference
	private PanelDisplayService panelDisplayService;

	public Tile tileManagementTile = getValue(() -> {
		Panel panel = panelDisplayService.newPanel();
		panel.titleProperty().bind(I18N.localize("org.to2mbn.lolixl.ui.impl.container.tiles.management.title"));
		panel.iconProperty().set(null); // TODO
		panel.contentProperty().set(tileManagingPresenter.getView().rootContainer);
		panel.onShownProperty().set(tileManagingPresenter::onShown);
		Tile tile = new Tile();
		tile.setOnAction(event -> panel.show());
		tile.setGraphic(new ImageView()); // TODO
		return tile;
	});

	@Override
	public void postInitialize() {
		view.tileRootContainer.setBottom(tileManagementTile);
	}

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
	}

	private <T> T getValue(Supplier<T> supplier) {
		return supplier.get();
	}
}