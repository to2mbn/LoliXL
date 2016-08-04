package org.to2mbn.lolixl.ui.impl.container.presenter;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
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
import org.to2mbn.lolixl.utils.MappedObservableList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

@Service({ HomeContentPresenter.class })
@Component(immediate = true)
public class HomeContentPresenter extends Presenter<HomeContentView> {

	private static final String FXML_LOCATION = "/ui/fxml/container/home_content.fxml";

	private static final Logger LOGGER = Logger.getLogger(HomeContentPresenter.class.getCanonicalName());

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
			TileAnimationHandler animationHandler = new TileAnimationHandler(tile, view.tileContainer);
			tile.addEventHandler(MouseEvent.MOUSE_ENTERED, animationHandler::runRollOutAnimation);
			tile.addEventHandler(MouseEvent.MOUSE_EXITED, animationHandler::cancelAndFallback);
			tile.setPrefWidth(60);
			tile.resize(60, 60);
			tile.setPadding(Insets.EMPTY);
			return tile;
		});
		tilesMapping.addListener(
				(observable, oldValue, newValue) -> view.tileContainer.getChildren().setAll(newValue));

	}

	/**
	 * Lazy-bind for tileManagementTile.
	 *
	 * @param tile
	 */
	public void setManagementTile(Tile tile) {
		view.tileRootContainer.setBottom(tile);
	}

	private static class TileAnimationHandler {
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
				time = Duration.millis(500);
			}
			Timeline newOne = new Timeline(
					new KeyFrame(Duration.ZERO, new KeyValue(tile.prefWidthProperty(), tile.getPrefWidth())),
					new KeyFrame(time, new KeyValue(tile.prefWidthProperty(), 300))
			);
			newOne.setOnFinished(event -> currentAnimation.set(null));
			currentAnimation.set(newOne);
			newOne.play();
		}

		private void cancelAndFallback(MouseEvent mouseEvent) {
			LOGGER.info("fall back!!!!");
			Duration time;
			Timeline current = currentAnimation.get();
			if (current != null) {
				time = current.getCurrentTime();
				currentAnimation.set(null);
				current.stop();
			} else {
				time = Duration.millis(500);
			}
			Timeline newOne = new Timeline(
					new KeyFrame(Duration.ZERO, new KeyValue(tile.prefWidthProperty(), tile.getPrefWidth())),
					new KeyFrame(time, new KeyValue(tile.prefWidthProperty(), 60))
			);
			newOne.setOnFinished(event -> currentAnimation.set(null));
			currentAnimation.set(newOne);
			newOne.play();
		}
	}
}
