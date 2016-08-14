package org.to2mbn.lolixl.ui.impl.container.presenter;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.ui.SideBarTileService;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.view.HomeContentView;
import org.to2mbn.lolixl.ui.model.SidebarTileElement;
import org.to2mbn.lolixl.utils.MappedObservableList;
import java.util.concurrent.atomic.AtomicReference;

@Service({ HomeContentPresenter.class })
@Component(immediate = true)
public class HomeContentPresenter extends Presenter<HomeContentView> {

	private static final String FXML_LOCATION = "/ui/fxml/container/home_content.fxml";

	private static final String CSS_CLASS_TILE = "xl-sidebar-tile";
	private static final String CSS_CLASS_TILE_EXPANDED = "xl-sidebar-tile-expanded";
	private static final String CSS_CLASS_TILE_UNEXPANDED = "xl-sidebar-tile-unexpanded";
	private static final String CSS_CLASS_TILE_EXPANDING = "xl-sidebar-tile-expanding";

	// Magic numbers
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
		Bindings.bindContent(view.tileContainer.getChildren(), tilesMapping);
		view.startGameButton.textProperty().bind(I18N.localize("org.to2mbn.lolixl.ui.impl.container.presenter.homecontent.launch_button.text"));
	}

	/**
	 * Lazy-bind for tileManagementTile.
	 *
	 * @param tile
	 */
	public void setManagementTile(Tile tile) {
		tile.getStyleClass().add("xl-sidebar-tile-management");
		view.tileRootContainer.setBottom(tile);
	}

	private void resolveTile(Tile tile) {
		TileAnimationHandler animationHandler = new TileAnimationHandler(tile);
		tile.addEventHandler(MouseEvent.MOUSE_ENTERED, animationHandler::runRollOutAnimation);
		tile.addEventHandler(MouseEvent.MOUSE_EXITED, animationHandler::cancelAndFallback);
		tile.getStyleClass().add(CSS_CLASS_TILE);
		tile.getStyleClass().add(CSS_CLASS_TILE_UNEXPANDED);
	}

	private class TileAnimationHandler {

		private final Tile tile;
		private final AtomicReference<Timeline> currentAnimation = new AtomicReference<>(null);

		private TileAnimationHandler(Tile _tile) {
			tile = _tile;
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
					new KeyFrame(time, new KeyValue(tile.prefWidthProperty(), -1))); // FIXME: use a fit width here
			newOne.setOnFinished(event -> {
				currentAnimation.set(null);
				tile.getStyleClass().remove(CSS_CLASS_TILE_EXPANDING);
				tile.getStyleClass().add(CSS_CLASS_TILE_EXPANDED);
			});
			currentAnimation.set(newOne);
			tile.getStyleClass().remove(CSS_CLASS_TILE_UNEXPANDED);
			tile.getStyleClass().add(CSS_CLASS_TILE_EXPANDING);
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
					new KeyFrame(time, new KeyValue(tile.prefWidthProperty(), tile.getPrefHeight()))); // let width be the same as height
			newOne.setOnFinished(event -> {
				currentAnimation.set(null);
				tile.getStyleClass().remove(CSS_CLASS_TILE_EXPANDING);
				tile.getStyleClass().add(CSS_CLASS_TILE_UNEXPANDED);
			});
			currentAnimation.set(newOne);
			tile.getStyleClass().remove(CSS_CLASS_TILE_EXPANDED);
			tile.getStyleClass().add(CSS_CLASS_TILE_EXPANDING);
			newOne.play();
		}
	}
}
