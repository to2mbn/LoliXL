package org.to2mbn.lolixl.ui.impl.container.presenter.content;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.to2mbn.lolixl.ui.Panel;
import org.to2mbn.lolixl.ui.PanelDisplayService;
import org.to2mbn.lolixl.ui.TileManagingService;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.view.content.HomeContentView;

import java.io.IOException;

@Component
public class HomeContentPresenter extends Presenter<HomeContentView> implements TileManagingService {
	private static final String LOCATION_OF_FXML = "/ui/fxml/container/home_content.fxml";

	@Reference
	private PanelDisplayService displayService;

	public void initialize() throws IOException {
		super.initialize(LOCATION_OF_FXML);
		AnchorPane.setRightAnchor(view.startGameButton, 0D);
		// TODO: Start game button & 'More' tile
		view.settingsTile.setOnAction(event -> {
			Panel panel = displayService.newPanel();
			panel.setTitle("设置");
			panel.setContent(new Pane());
			panel.show();
		});
	}

	@Override
	public void addTileForPanel(Panel panel) {

	}

	@Override
	public void removeTile(Panel panel) {

	}

	@Override
	public void setSize(int size) {

	}
}
