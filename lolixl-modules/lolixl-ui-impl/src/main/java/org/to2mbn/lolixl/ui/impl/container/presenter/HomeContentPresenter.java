package org.to2mbn.lolixl.ui.impl.container.presenter;

import javafx.scene.image.ImageView;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.ui.Panel;
import org.to2mbn.lolixl.ui.PanelDisplayService;
import org.to2mbn.lolixl.ui.SideBarTileService;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panel.downloads.DownloadCenterPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panel.settings.SettingsPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panel.tiles.TileManagingPresenter;
import org.to2mbn.lolixl.ui.impl.container.view.HomeContentView;
import java.util.function.Supplier;

/**
 * 设计之路真是任重而道远啊
 */
//设你妈的计 -- yushijinhun
@Component
public class HomeContentPresenter extends Presenter<HomeContentView> {

	private static final String FXML_LOCATION = "/ui/fxml/container/home_content.fxml";

	@Reference //TODO
	private DownloadCenterPresenter downloadCenterPresenter;

	@Reference // TODO
	private SettingsPresenter settingsPresenter;

	@Reference
	private TileManagingPresenter tileManagingPresenter;

	@Reference
	private SideBarTileService tileService;

	@Reference
	private PanelDisplayService panelDisplayService;

	public Tile downloadCenterTile = getValue(() -> {
		Panel panel = panelDisplayService.newPanel();
		panel.titleProperty().bind(I18N.localize("org.to2mbn.lolixl.ui.impl.container.downloads.title"));
		panel.iconProperty().set(null); // TODO
		panel.contentProperty().set(downloadCenterPresenter.getView().rootContainer);
		panel.onShownProperty().set(downloadCenterPresenter::startUpdateCycle);
		panel.onClosedProperty().set(downloadCenterPresenter::resumeUpdateCycle);
		Tile tile = new Tile();
		tile.setOnAction(event -> panel.show());
		tile.setGraphic(new ImageView()); // TODO
		return tile;
	});

	public Tile settingsTile = getValue(() -> {
		Panel panel = panelDisplayService.newPanel();
		panel.titleProperty().bind(I18N.localize("org.to2mbn.lolixl.ui.impl.container.settings.title"));
		panel.iconProperty().set(null); // TODO
		panel.contentProperty().set(settingsPresenter.getView().rootContainer);
		panel.onShownProperty().set(settingsPresenter::onShown);
		Tile tile = new Tile();
		tile.setOnAction(event -> panel.show());
		tile.setGraphic(new ImageView()); // TODO
		return tile;
	});

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
		view.tileContainer.getChildren().addAll(downloadCenterTile, settingsTile);
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