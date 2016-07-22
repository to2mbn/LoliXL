package org.to2mbn.lolixl.ui.impl.container.presenter.content;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import org.apache.felix.scr.annotations.Component;
import org.to2mbn.lolixl.ui.Panel;
import org.to2mbn.lolixl.ui.TileManagingService;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.component.TileImpl;
import org.to2mbn.lolixl.ui.impl.container.view.content.HomeContentView;
import org.to2mbn.lolixl.utils.FXUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Component
public class HomeContentPresenter extends Presenter<HomeContentView> implements TileManagingService {
	private static final String LOCATION_OF_FXML = "/ui/fxml/container/home_content.fxml";

	private final List<Node> tiles = view.tileContainer.getChildren();
	private final List<Node> hiddenTiles = new LinkedList<>();

	private int tileSize = 60;

	public void initialize() throws IOException {
		super.initialize(LOCATION_OF_FXML);
		AnchorPane.setRightAnchor(view.startGameButton, 0D);
		view.tileContainer.getChildren().remove(view.moreTilesTile); // 默认不显示"..."磁贴
		// TODO: Start game button & 'More' tile
		setSize(tileSize);

		// TODO 默认的磁贴
	}

	@Override
	public void addTile(Tile tile) {
		Objects.requireNonNull(tile);
		tiles.add((Button) tile);
	}

	@Override
	public Tile newTile() {
		return new TileImpl();
	}

	@Override
	public void addTileForPanel(Panel panel) {
		Objects.requireNonNull(panel);
		Tile tile = newTile();
		tile.setIcon(panel.getIcon());
		tile.setText(panel.getTitle());
		tile.setOnClicked(event -> panel.show());
		addTile(tile);
	}

	@Override
	public void removeTile(Panel panel) {
		Objects.requireNonNull(panel);
		tiles.remove(panel);
	}

	@Override
	public void setSize(int size) {
		FXUtils.checkFxThread();
		if (size > TILE_MAX_SIZE) {
			throw new IllegalArgumentException("size can not be bigger than 60");
		}
		if (checkIfOverFull(size)) {
			do {
				removeLastTile();
			} while (checkIfOverFull(size));
			removeLastTile(); // 再删除一个是为了给"..."磁贴留位置
			tiles.add(view.moreTilesTile);
		} else {
			tiles.forEach(tile -> tile.resize(size, size));
		}
		tileSize = size;
	}

	@Override
	public int getSize() {
		return 0;
	}

	@Override
	public Tile[] getTiles(TileStatus status) {
		Stream<Node> stream;
		switch (status) {
			case SHOWN:
				List<Node> copyAll = new LinkedList<>(tiles);
				copyAll.removeAll(hiddenTiles);
				stream = copyAll.stream();
				break;
			case HIDDEN:
				stream = hiddenTiles.stream();
				break;
			case COMMON:
			default:
				stream = tiles.stream();
				break;
		}
		return stream
				.map(node -> (Tile) node)
				.toArray(Tile[]::new);
	}

	@Override
	public void updateTilesOrder(Tile[] tiles) {
		Objects.requireNonNull(tiles);
		FXUtils.checkFxThread();
		// TODO
	}

	private boolean checkIfOverFull(int size) {
		int total = 0;
		for (int i = 0; i < view.tileContainer.getChildren().size(); i++) {
			total += size + view.tileContainer.getSpacing();
		}
		total -= view.tileContainer.getSpacing();
		return view.tileContainer.getHeight() < total;
	}

	private void removeLastTile() {
		Node last = tiles.get(tiles.size() - 1);
		tiles.remove(last);
	}
}
