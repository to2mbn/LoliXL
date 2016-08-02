package org.to2mbn.lolixl.ui.impl.container.presenter.panel.downloads;

import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.layout.Region;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.framework.BundleContext;
import org.to2mbn.lolixl.core.download.notify.DownloadCenterNotifier;
import org.to2mbn.lolixl.core.download.notify.DownloadTaskGroup;
import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.ui.Panel;
import org.to2mbn.lolixl.ui.PanelDisplayService;
import org.to2mbn.lolixl.ui.PresenterManagementService;
import org.to2mbn.lolixl.ui.SideBarAlertService;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.component.view.downloads.DownloadCenterAlertView;
import org.to2mbn.lolixl.ui.impl.component.view.downloads.DownloadTaskGroupItemInfoView;
import org.to2mbn.lolixl.ui.impl.component.view.downloads.DownloadTaskGroupItemView;
import org.to2mbn.lolixl.ui.impl.container.presenter.HomeContentPresenter;
import org.to2mbn.lolixl.ui.impl.container.view.panel.downloads.DownloadCenterView;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class DownloadCenterPresenter extends Presenter<DownloadCenterView> {
	private static final Logger LOGGER = Logger.getLogger(DownloadCenterPresenter.class.getCanonicalName());
	private static final String FXML_LOCATION = "/ui/fxml/panel/download_center_panel.fxml";

	@Reference
	private PanelDisplayService displayService;

	@Reference
	private DownloadCenterNotifier downloadCenterNotifier;

	@Reference
	private SideBarAlertService alertService;

	@Reference
	private PresenterManagementService presenterService;

	private final ReadOnlyIntegerWrapper taskCountProperty;

	public DownloadCenterPresenter(BundleContext ctx) {
		super(ctx);
		taskCountProperty = new ReadOnlyIntegerWrapper(0);
	}

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
	}

	private Timer timer;
	private TimerTask updateTask;
	private Map<DownloadTaskGroup, DownloadTaskGroupItemView> itemMapping;

	@Override
	public void postInitialize() {
		timer = new Timer(false);
		updateTask = new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(DownloadCenterPresenter.this::updateStatus);
			}
		};
		itemMapping = new ConcurrentHashMap<>();
		registerAlert();
	}

	public void startUpdateCycle() {
		timer.schedule(updateTask, 0, 100);
	}

	public void resumeUpdateCycle() {
		timer.cancel();
		itemMapping.forEach(((group, view) -> view.resumeUpdateCycle()));
	}

	public ReadOnlyIntegerProperty taskCountProperty() {
		return taskCountProperty.getReadOnlyProperty();
	}

	private void addItem(DownloadTaskGroup group) throws IOException {
		Tile tile = new Tile();
		tile.setPrefWidth(Region.USE_COMPUTED_SIZE);
		tile.setPrefHeight(60D);
		tile.prefWidthProperty().bind(view.itemContainer.widthProperty().subtract(view.itemContainer.getPadding().getRight()));
		DownloadTaskGroupItemView itemView = new DownloadTaskGroupItemView(group);
		DownloadTaskGroupItemInfoView infoView = new DownloadTaskGroupItemInfoView(group);
		tile.setGraphic(itemView);
		tile.setOnAction(event -> {
			Panel panel = displayService.newPanel();
			panel.titleProperty().bind(itemView.nameLabel.textProperty());
			panel.contentProperty().set(infoView);
			panel.onShownProperty().set(infoView::startUpdateCycle);
			panel.onClosedProperty().set(infoView::resumeUpdateCycle);
			panel.show();
		});
		// view.itemContainer.setPrefHeight(view.itemContainer.getPrefHeight() + tile.getPrefHeight() + view.itemContainer.getSpacing());
		view.itemContainer.getChildren().add(tile);
		itemMapping.put(group, itemView);
		itemView.startUpdateCycle();
		synchronized (taskCountProperty) {
			taskCountProperty.set(taskCountProperty.get() + 1);
		}
	}

	private void tryAddItem(DownloadTaskGroup group) {
		try {
			addItem(group);
		} catch (IOException ex) {
			LOGGER.log(Level.WARNING, "Failed to add item for download task group, this should not be happened", ex);
		}
	}

	private void updateStatus() {
		if (itemMapping.isEmpty()) {
			downloadCenterNotifier.forEachChangedTask(this::tryAddItem, true);
		} else {
			downloadCenterNotifier.forEachChangedTask(entry -> {
				if (!itemMapping.containsKey(entry)) {
					tryAddItem(entry);
				} else if (entry.isDone()) {
					synchronized (taskCountProperty) {
						taskCountProperty.set(taskCountProperty.get() - 1);
					}
				}
			}, false);
		}
	}

	private void registerAlert() {
		DownloadCenterAlertView view;
		try {
			view = new DownloadCenterAlertView();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		Tile downloadCenterTile = presenterService.getPresenter(HomeContentPresenter.class).downloadCenterTile;
		view.iconView.imageProperty(); // TODO
		view.msgLabel.textProperty().bind(new StringBinding() {
			ObservableStringValue i18n =
					I18N.localize("org.to2mbn.lolixl.ui.impl.container.presenter.panel.downloads.alert.msg", taskCountProperty.getReadOnlyProperty());

			{
				bind(i18n);
			}

			@Override
			protected String computeValue() {
				return i18n.get();
			}
		});
		Tile alertTile = new Tile();
		alertTile.setGraphic(view);
		alertTile.setOnAction(downloadCenterTile.getOnAction()::handle);
		alertService.addAlert(alertTile);
	}
}
