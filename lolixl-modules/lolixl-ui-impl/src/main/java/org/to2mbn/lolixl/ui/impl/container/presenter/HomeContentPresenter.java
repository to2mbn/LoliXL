package org.to2mbn.lolixl.ui.impl.container.presenter;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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
			TileAnimationHandler animationHandler = new TileAnimationHandler(tile);
			tile.addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
				if (!animationHandler.isPlaying.get()) {
					animationHandler.runRollOutAnimation();
				}
			});
			tile.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
				if (animationHandler.isPlaying.get()) {
					animationHandler.cancelAndFallback();
				}
			});
			tile.setLayoutX(tile.getLayoutX() + tile.getWidth() - tile.getHeight());
			LOGGER.fine("Mapping tile [" + tile.getId() + "]");
			return tile;
		});
		tilesMapping.addListener(((observable, oldValue, newValue) -> {
			List<Node> children = view.tileContainer.getChildren();
			children.clear();
			children.addAll(newValue);
		}));
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
		private final AtomicBoolean isPlaying = new AtomicBoolean(false);
		private final AtomicReference<Transition> currentAnimation = new AtomicReference<>(null);

		private TileAnimationHandler(Tile _tile) {
			tile = _tile;
		}

		private void runRollOutAnimation() {
			TranslateTransition tran = new TranslateTransition(Duration.millis(800), tile);
			double x = tile.getLayoutX();
			tran.setFromX(x + tile.getWidth());
			tran.setToX(x);
			tran.setOnFinished(event -> isPlaying.set(false));
			currentAnimation.set(tran);
			isPlaying.set(true);
			tran.play();
		}

		private void cancelAndFallback() {
			Transition tran = currentAnimation.get();
			if (tran != null) {
				Duration time = tran.getCurrentTime();
				tran.stop();
				tran.setInterpolator(new Interpolator() {
					@Override
					protected double curve(double t) {
						return -t;
					}
				});
				tran.play();
				tran.jumpTo(tran.getTotalDuration().subtract(time));
			}
		}
	}
}
