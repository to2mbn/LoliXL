package org.to2mbn.lolixl.ui.impl.container.presenter;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.WeakEventHandler;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.ui.SideBarTileService;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.view.HomeContentView;
import org.to2mbn.lolixl.ui.model.SidebarTileElement;
import org.to2mbn.lolixl.utils.CollectionUtils;
import org.to2mbn.lolixl.utils.MappedObservableList;
import java.util.concurrent.atomic.AtomicReference;

@Service({ HomeContentPresenter.class })
@Component(immediate = true)
public class HomeContentPresenter extends Presenter<HomeContentView> {

	private static final String FXML_LOCATION = "/ui/fxml/container/home_content.fxml";

	private int tileAnimationDuration = 100;

	@Reference
	private SideBarTileService tileService;

	private MappedObservableList<SidebarTileElement, Tile> tilesMapping;

	@Activate
	public void active(ComponentContext compCtx) {
		super.active();
	}

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
	}

	@Override
	protected void initializePresenter() {
		tilesMapping = new MappedObservableList<>(tileService.getTiles(SideBarTileService.StackingStatus.SHOWN), element -> {
			Tile tile = element.createTile();
			resolveTile(tile);
			return tile;
		});
		CollectionUtils.bindList(tilesMapping, view.tileContainer.getChildren());
	}

	/**
	 * Lazy-bind for tileManagementTile.
	 *
	 * @param tile
	 */
	public void setManagementTile(Tile tile) {
		resolveTile(tile);
		view.tileRootContainer.setBottom(tile);
	}

	private void resolveTile(Tile tile) {
		TileAnimationHandler animationHandler = new TileAnimationHandler(tile, view.tileContainer);
		tile.addEventHandler(MouseEvent.MOUSE_ENTERED, new WeakEventHandler<>(animationHandler::runRollOutAnimation));
		tile.addEventHandler(MouseEvent.MOUSE_EXITED, new WeakEventHandler<>(animationHandler::cancelAndFallback));
		tile.setPrefWidth(55);
		tile.resize(55, 55);
		tile.setPadding(Insets.EMPTY);
	}

	private class TileAnimationHandler {
		private final Tile tile;
		private final Region tileContainer;
		private final AtomicReference<Timeline> currentAnimation = new AtomicReference<>(null);

		private TileAnimationHandler(Tile _tile, Region _tileContainer) {
			tile = _tile;
			tileContainer = _tileContainer;
		}

		private void runRollOutAnimation(MouseEvent mouseEvent) {
			Duration time;
			Timeline current = currentAnimation.get();
			if (current != null) {
				time = current.getTotalDuration().subtract(current.getCurrentTime());
				currentAnimation.set(null);
				current.stop();
			} else {
				time = Duration.millis(tileAnimationDuration);
			}
			Timeline newOne = new Timeline(
					new KeyFrame(Duration.ZERO, new KeyValue(tile.prefWidthProperty(), tile.getPrefWidth())),
					new KeyFrame(time, new KeyValue(tile.prefWidthProperty(), 200))
			);
			newOne.setOnFinished(event -> currentAnimation.set(null));
			currentAnimation.set(newOne);
			newOne.play();
		}

		private void cancelAndFallback(MouseEvent mouseEvent) {
			Duration time;
			Timeline current = currentAnimation.get();
			if (current != null) {
				time = current.getCurrentTime();
				currentAnimation.set(null);
				current.stop();
			} else {
				time = Duration.millis(tileAnimationDuration);
			}
			Timeline newOne = new Timeline(
					new KeyFrame(Duration.ZERO, new KeyValue(tile.prefWidthProperty(), tile.getPrefWidth())),
					new KeyFrame(time, new KeyValue(tile.prefWidthProperty(), 55))
			);
			newOne.setOnFinished(event -> currentAnimation.set(null));
			currentAnimation.set(newOne);
			newOne.play();
		}
	}
}
