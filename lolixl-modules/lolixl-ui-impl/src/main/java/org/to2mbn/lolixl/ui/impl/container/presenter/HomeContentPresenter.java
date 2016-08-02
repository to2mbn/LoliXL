package org.to2mbn.lolixl.ui.impl.container.presenter;

import javafx.scene.image.ImageView;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.framework.BundleContext;
import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.ui.Panel;
import org.to2mbn.lolixl.ui.PanelDisplayService;
import org.to2mbn.lolixl.ui.PresenterManagementService;
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
@Component
public class HomeContentPresenter extends Presenter<HomeContentView> {
	private static final String LOCATION_OF_FXML = "/ui/fxml/container/home_content.fxml";

	@Reference
	private SideBarTileService tileService;

	@Reference
	private PanelDisplayService panelDisplayService;

	@Reference
	private PresenterManagementService presenterService;

	public Tile downloadCenterTile = getValue(() -> {
		DownloadCenterPresenter downloadCenterPresenter = presenterService.getPresenter(DownloadCenterPresenter.class);
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
		SettingsPresenter settingsPresenter = presenterService.getPresenter(SettingsPresenter.class);
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
		TileManagingPresenter tileManagingPresenter = presenterService.getPresenter(TileManagingPresenter.class);
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

	public HomeContentPresenter(BundleContext ctx) {
		super(ctx);
	}

	@Override
	public void postInitialize() {
		view.tileContainer.getChildren().addAll(downloadCenterTile, settingsTile);
		view.tileRootContainer.setBottom(tileManagementTile);
	}

	@Override
	protected String getFxmlLocation() {
		return LOCATION_OF_FXML;
	}

	private <T> T getValue(Supplier<T> supplier) {
		return supplier.get();
	}
}