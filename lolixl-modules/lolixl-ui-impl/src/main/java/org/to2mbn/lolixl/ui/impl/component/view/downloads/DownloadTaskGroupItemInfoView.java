package org.to2mbn.lolixl.ui.impl.component.view.downloads;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableListValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import org.to2mbn.lolixl.core.impl.download.notify.DownloadTaskEntry;
import org.to2mbn.lolixl.core.impl.download.notify.DownloadTaskGroup;
import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.utils.BundleUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Timer;
import java.util.TimerTask;

public class DownloadTaskGroupItemInfoView extends BorderPane {
	private static final String FXML_LOCATION = "/ui/fxml/component/download_group_info_panel.fxml";

	@FXML
	public BorderPane headerContainer;

	@FXML
	public ListView<DownloadTaskEntry> itemContainer;

	@FXML
	public Button cancelButton;

	@FXML
	public ProgressBar progressBar;

	private final Timer timer;
	private final TimerTask updateTask;
	private final DownloadTaskGroup taskGroup;
	private final ObservableListValue<DownloadTaskEntry> entries;

	public DownloadTaskGroupItemInfoView(DownloadTaskGroup group) {
		timer = new Timer(true);
		updateTask = new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(DownloadTaskGroupItemInfoView.this::updateStatus);
			}
		};
		taskGroup = group;
		entries = new SimpleListProperty<>(FXCollections.observableArrayList());
		FXMLLoader loader = new FXMLLoader(BundleUtils.getResourceFromBundle(getClass(), FXML_LOCATION));
		loader.setRoot(this);
		loader.setController(this);
		try {
			loader.load();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		itemContainer.setCellFactory(view -> new ListCell<DownloadTaskEntry>() {
			@Override
			public void updateItem(DownloadTaskEntry entry, boolean empty) {
				super.updateItem(entry, empty);
				setGraphic(makePaneForEntry(entry));
			}
		});
		cancelButton.textProperty().bind(I18N.localize("org.to2mbn.lolixl.ui.impl.component.view.downloads.item.cancelbutton.text"));
		cancelButton.setOnAction(event -> group.cancel(true));
		startUpdateCycle();
	}

	public void resumeUpdateCycle() {
		timer.cancel();
	}

	public void startUpdateCycle() {
		timer.schedule(updateTask, 0, 100);
	}

	private void updateStatus() {
		if (entries.size() < 1) {
			itemContainer.itemsProperty().bind(entries);
			taskGroup.forEachChangedEntry(entries::add, true);
		} else {
			taskGroup.forEachChangedEntry(entry -> {
				if (entries.contains(entry)) {
					entries.set(entries.indexOf(entry), entry);
				} else {
					entries.add(entry);
				}
			}, false);
		}
		progressBar.setProgress(taskGroup.getFinishedCount() / taskGroup.getTotalCount());
	}

	private AnchorPane makePaneForEntry(DownloadTaskEntry entry) {
		AnchorPane pane = new AnchorPane();
		Label nameLabel = new Label();
		nameLabel.setId("-xl-download-task-group-item-info-item-name-label");
		nameLabel.setText(String.format("[%s/%s]%s", entry.getLastRetry().getCurrent(), entry.getLastRetry().getMax(), entry.getTask().getURI()));
		Label statusLabel = new Label();
		statusLabel.setId("-xl-download-task-group-item-info-item-status-label");
		StringProperty text = statusLabel.textProperty();
		if (entry.isCancelled()) {
			text.bind(I18N.localize("org.to2mbn.lolixl.ui.impl.component.view.downloads.item.status.cancelled"));
		} else if (entry.getException() != null) { // 切不可改变判断顺序
			text.bind(I18N.localize("org.to2mbn.lolixl.ui.impl.component.view.downloads.item.status.error"));
		} else if (entry.isDone()) {
			text.bind(I18N.localize("org.to2mbn.lolixl.ui.impl.component.view.downloads.item.status.done"));
		} else {
			text.set((entry.getProgress().getDone() / entry.getProgress().getTotal()) + "%");
		}
		return pane;
	}
}
