package org.to2mbn.lolixl.ui.impl.pages.download;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.core.impl.download.notify.DownloadCenterNotifier;
import org.to2mbn.lolixl.core.impl.download.notify.DownloadTaskGroup;
import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.ui.Presenter;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.image.ImageLoading;
import org.to2mbn.lolixl.ui.panel.Panel;
import org.to2mbn.lolixl.ui.panel.PanelDisplayService;
import org.to2mbn.lolixl.ui.sidebar.SidebarTileElement;
import org.to2mbn.lolixl.utils.FXUtils;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

//TODO: 在重构完毕后令enabled=true
@Service({ SidebarTileElement.class })
@Component(immediate = true, enabled = false)
public class DownloadCenterPresenter extends Presenter<DownloadCenterView> implements SidebarTileElement {

	private static final String FXML_LOCATION = "fxml/org.to2mbn.lolixl.ui.download_center/download_center_panel.fxml";

	@Reference
	private PanelDisplayService displayService;

	@Reference
	private DownloadCenterNotifier downloadCenterNotifier;

	private final ReadOnlyIntegerWrapper taskCountProperty = new ReadOnlyIntegerWrapper(0);

	private AtomicReference<Timer> currentTimer = new AtomicReference<>(null);
	private Map<DownloadTaskGroup, DownloadTaskGroupItemView> itemMapping = new ConcurrentHashMap<>();

	@Activate
	public void active(ComponentContext compCtx) {
		super.active();
	}

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
	}

	@Override
	protected void initializePresenter() {}

	public void startUpdateCycle() {
		Timer timer = new Timer(true);
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				Platform.runLater(DownloadCenterPresenter.this::updateStatus);
			}
		}, 0, 100);
		currentTimer.set(timer);
	}

	public void resumeUpdateCycle() {
		if (currentTimer.get() != null) {
			currentTimer.get().cancel();
			currentTimer.set(null);
		}
		itemMapping.forEach(((group, view) -> view.resumeUpdateCycle()));
	}

	public ReadOnlyIntegerProperty taskCountProperty() {
		return taskCountProperty.getReadOnlyProperty();
	}

	private void addItem(DownloadTaskGroup group) {
		Tile tile = new Tile();
		tile.setPrefWidth(Region.USE_COMPUTED_SIZE);
		tile.setPrefHeight(60D);
		tile.prefWidthProperty().bind(view.itemsContainer.widthProperty().subtract(view.itemsContainer.getPadding().getRight()));
		DownloadTaskGroupItemView itemView = new DownloadTaskGroupItemView(group);
		DownloadTaskGroupItemInfoView infoView = new DownloadTaskGroupItemInfoView(group);
		FXUtils.setButtonGraphic(tile, itemView);
		tile.setOnAction(event -> {
			Panel panel = displayService.newPanel();
			panel.titleProperty().bind(itemView.nameLabel.textProperty());
			panel.contentProperty().set(infoView);
			panel.onShownProperty().set(infoView::startUpdateCycle);
			panel.onClosedProperty().set(infoView::resumeUpdateCycle);
			panel.show();
		});
		// view.itemContainer.setPrefHeight(view.itemContainer.getPrefHeight() + tile.getPrefHeight() + view.itemContainer.getSpacing());
		view.itemsContainer.getChildren().add(tile);
		itemMapping.put(group, itemView);
		itemView.startUpdateCycle();
		synchronized (taskCountProperty) {
			taskCountProperty.set(taskCountProperty.get() + 1);
		}
	}

	private void tryAddItem(DownloadTaskGroup group) {
		addItem(group);
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

	@Override
	public ObservableStringValue getLocalizedName() {
		return I18N.localize("org.to2mbn.lolixl.ui.download_center.title");
	}

	@Override
	public Tile createTile() {
		Tile tile = SidebarTileElement.super.createTile();

		Panel panel = displayService.newPanel();
		panel.bindItem(this);
		panel.bindButton(tile);

		panel.contentProperty().set(view.rootContainer);
		panel.onShownProperty().set(this::startUpdateCycle);
		panel.onClosedProperty().set(this::resumeUpdateCycle);

		return tile;
	}

	@Override
	public ObservableObjectValue<Image> getIcon() {
		return ImageLoading.load("img/org.to2mbn.lolixl.ui.download_center/icon.png");
	}

}
