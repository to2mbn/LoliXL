package org.to2mbn.lolixl.ui.impl.container.presenter.content;

import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundSize;
import org.apache.felix.scr.annotations.Component;
import org.to2mbn.lolixl.ui.Panel;
import org.to2mbn.lolixl.ui.TileManagingService;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.DefaultFramePresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panelcontent.HiddenTilesPanelContentPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panelcontent.TileManagingPanelContentPresenter;
import org.to2mbn.lolixl.ui.impl.container.view.content.HomeContentView;
import org.to2mbn.lolixl.utils.FXUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Component
public class HomeContentPresenter extends Presenter<HomeContentView> implements TileManagingService {
	private static final String LOCATION_OF_FXML = "/ui/fxml/container/home_content.fxml";

	private int tileSize = 60;

	private DefaultFramePresenter defaultFramePresenter;
	private HiddenTilesPanelContentPresenter hiddenTilesPanelContentPresenter;
	private TileManagingPanelContentPresenter tileManagingPanelContentPresenter;

	private List<Node> shownTiles;
	private List<Node> hiddenTiles;

	public Supplier<Tile> hiddenTilesPanel = () -> {
		Panel panel = defaultFramePresenter.newPanel();
		panel.setContent(hiddenTilesPanelContentPresenter.getView().tilesContainer);
		return newTileForPanel(panel);
	};

	public Supplier<Tile> manageTilesPanel = () -> {
		Panel panel = defaultFramePresenter.newPanel();
		panel.setContent(tileManagingPanelContentPresenter.getView().rootContainer);
		return newTileForPanel(panel);
	};

	@Override
	public void postInitialize() {
		shownTiles = view.tileContainer.getChildren();
		setSize(tileSize);
		addTile(manageTilesPanel.get());
		// TODO: Start game button
		view.tileContainer.heightProperty().addListener(this::onTileContainerChanged);
	}

	@Override
	protected String getFxmlLocation() {
		return LOCATION_OF_FXML;
	}

	public void setHiddenTilesPanelContentPresenter(HiddenTilesPanelContentPresenter presenter) {
		hiddenTilesPanelContentPresenter = presenter;
		hiddenTiles = presenter.getView().tilesContainer.getChildren();
	}

	public void setTileManagingPanelContentPresenter(TileManagingPanelContentPresenter _tileManagingPanelContentPresenter) {
		tileManagingPanelContentPresenter = _tileManagingPanelContentPresenter;
	}

	public void setDefaultFramePresenter(DefaultFramePresenter _defaultFramePresenter) {
		defaultFramePresenter = _defaultFramePresenter;
	}

	@Override
	public void addTile(Tile tile) {
		Objects.requireNonNull(tile);
		FXUtils.checkFxThread();
		if (checkIfOverFull(tileSize, false)) {
			hiddenTiles.add(tile);
		} else if (checkIfOverFull(tileSize, true)) {
			hiddenTiles.add(tile);
			shownTiles.add(hiddenTilesPanel.get());
		} else {
			// 将"管理磁贴"磁贴放到最后一位
			int index = shownTiles.indexOf(manageTilesPanel);
			shownTiles.add(index, tile);
		}
	}

	@Override
	public void addTileForPanel(Panel panel) {
		Objects.requireNonNull(panel);
		FXUtils.checkFxThread();
		addTile(newTileForPanel(panel));
	}

	@Override
	public void removeTile(Panel panel) {
		Objects.requireNonNull(panel);
		FXUtils.checkFxThread();
		if (!shownTiles.remove(panel)) {
			hiddenTiles.remove(panel);
		}
		// 将被隐藏的首个磁贴添加到容器中显示
		if (!checkIfOverFull(tileSize, false) && hiddenTiles.size() > 0) {
			Node tile = hiddenTiles.get(0);
			hiddenTiles.remove(0);
			shownTiles.add(tile);
		}
	}

	@Override
	public void setSize(int size) {
		FXUtils.checkFxThread();
		if (size > TILE_MAX_SIZE) {
			throw new IllegalArgumentException("size can not be bigger than 60");
		}
		if (checkIfOverFull(size, false)) {
			do {
				hideLastTile();
			} while (checkIfOverFull(size, false));
			hideLastTile(); // 再隐藏一个是为了给"..."磁贴留位置
			addTile(hiddenTilesPanel.get());
		} else {
			shownTiles.forEach(tile -> tile.resize(size, size));
		}
		tileSize = size;
	}

	@Override
	public int getSize() {
		return tileSize;
	}

	@Override
	public Tile[] getTiles(TileStatus status) {
		Stream<Node> stream;
		switch (status) {
			case SHOWN:
				stream = shownTiles.stream();
				break;
			case HIDDEN:
				stream = hiddenTiles.stream();
				break;
			case COMMON:
			default:
				List<Node> copy = new LinkedList<>(shownTiles);
				copy.addAll(hiddenTiles);
				stream = copy.stream();
				break;
		}
		return stream
				.map(node -> (Tile) node)
				.toArray(Tile[]::new);
	}

	@Override
	public void updateTilesOrder(Tile[] newTiles) {
		Objects.requireNonNull(newTiles);
		FXUtils.checkFxThread();

		Tile[] allTiles = getTiles(TileStatus.COMMON);
		Stream<Tile> newTilesStream = Stream.of(newTiles);
		Stream<Tile> tileStream = Stream.of(allTiles);
		if (!tileStream.allMatch(tile -> newTilesStream.anyMatch(newTile -> tile.equals(newTile)))) {
			throw new IllegalArgumentException("tiles must contain all the tiles in the container");
		}

		// 先清除容器中的所有磁贴
		shownTiles.clear();
		hiddenTiles.clear();

		// 除去newTiles中不存在于tiles中的元素并添加到容器中
		newTilesStream
				.filter(newTile -> tileStream.anyMatch(tile -> tile.equals(newTile)))
				.forEach(this::addTile);
	}

	private boolean checkIfOverFull(int size, boolean pre) {
		int total = 0;
		for (int i = 0; i < view.tileContainer.getChildren().size() + (pre ? 1 : 0); i++) {
			total += size + view.tileContainer.getSpacing();
		}
		total -= view.tileContainer.getSpacing();
		return view.tileContainer.getHeight() < total;
	}

	private void hideLastTile() {
		Node last = shownTiles.get(shownTiles.size() - 1);
		hiddenTiles.add(last);
		shownTiles.remove(last);
	}

	private Tile newTileForPanel(Panel panel) {
		FXUtils.checkFxThread();
		Tile tile = new Tile();
		tile.resize(getSize(), getSize());
		tile.setText(panel.getTitle());
		tile.setOnAction(event -> panel.show());
		if (panel.getIcon() != null) {
			tile.setBackground(new Background(new BackgroundImage(panel.getIcon(), null, null, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
		}
		return tile;
	}

	private void onTileContainerChanged(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
		setSize(tileSize); // 刷新磁贴
	}
}