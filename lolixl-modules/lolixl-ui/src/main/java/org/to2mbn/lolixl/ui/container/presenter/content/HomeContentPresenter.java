package org.to2mbn.lolixl.ui.container.presenter.content;

import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import org.apache.felix.scr.annotations.Reference;
import org.to2mbn.lolixl.ui.PanelService;
import org.to2mbn.lolixl.ui.TileManagingService;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.container.view.content.HomeContentView;

import java.io.IOException;
import java.io.InputStream;

public class HomeContentPresenter extends Presenter<HomeContentView> implements TileManagingService {
	@Reference
	private PanelService displayService;

	@Override
	public void initialize(InputStream fxml) throws IOException {
		super.initialize(fxml);
		AnchorPane.setRightAnchor(view.startGameButton, 0D);
		// TODO: view.homeTile.setOnAction(event -> displayService.displayContent(view.rootContainer));
		// TODO: Start game button & 'More' tile
	}

	@Override
	public void addTile(Button tileButton) {
		view.tileContainer.getChildren().add(tileButton);
	}

	@Override
	public void removeTile(Button tileButton) {
		view.tileContainer.getChildren().remove(tileButton);
	}

	@Override
	public void setSize(int size) {

	}
}
