package org.to2mbn.lolixl.ui.impl.container.presenter;

import java.util.List;
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
import org.to2mbn.lolixl.utils.FunctionInterpolator;
import org.to2mbn.lolixl.utils.MappedObservableList;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.util.Duration;

@Service({ HomeContentPresenter.class })
@Component(immediate = true)
public class HomeContentPresenter extends Presenter<HomeContentView> {

	private static final String FXML_LOCATION = "/ui/fxml/container/home_content.fxml";

	private static final String CSS_CLASS_TILE = "xl-sidebar-tile";
	private static final String CSS_CLASS_TILE_EXPANDED = "xl-sidebar-tile-expanded";
	private static final String CSS_CLASS_TILE_UNEXPANDED = "xl-sidebar-tile-unexpanded";
	private static final String CSS_CLASS_TILE_EXPANDING = "xl-sidebar-tile-expanding";

	private int tileAnimationDuration = 300;

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
		tile.maxWidthProperty().set(Region.USE_PREF_SIZE);
		tile.maxHeightProperty().set(Region.USE_PREF_SIZE);
		tile.minWidthProperty().set(Region.USE_PREF_SIZE);
		tile.minHeightProperty().set(Region.USE_PREF_SIZE);
	}

	private class TileAnimationHandler {

		Interpolator interpolator = new FunctionInterpolator(t -> t <= 0.5 ? 4 * t * t * t : 4 * (t - 1) * (t - 1) * (t - 1) + 1);

		Tile tile;
		volatile Timeline current;

		TileAnimationHandler(Tile tile) {
			this.tile = tile;
		}

		Duration newAnimation() {
			Duration time;
			if (current != null) {
				time = current.getCurrentTime();
				current.stop();
				current = null;
			} else {
				time = Duration.millis(tileAnimationDuration);
			}
			return time;
		}

		void runRollOutAnimation(MouseEvent mouseEvent) {
			Duration time = newAnimation();

			// === calculate targetWidth
			setTileStateCssClass(tile, null);

			double originPrefWidth = tile.prefWidthProperty().get();
			Insets originPadding = tile.paddingProperty().get();

			tile.prefWidthProperty().set(-1);
			tile.contentDisplayProperty().set(ContentDisplay.RIGHT);
			tile.paddingProperty().set(new Insets(5, 5, 5, 10));

			double targetWidth = tile.prefWidth(-1);

			tile.prefWidthProperty().set(originPrefWidth);
			tile.paddingProperty().set(originPadding);

			// We don't restore contentDisplay in order to fix that
			// JFX doesn't apply css on contentDisplay until the animation finished.

			// ===

			setTileStateCssClass(tile, CSS_CLASS_TILE_EXPANDING);

			current = new Timeline(new KeyFrame(time, new KeyValue(tile.prefWidthProperty(), targetWidth, interpolator)));
			current.setOnFinished(event -> {
				current = null;
				setTileStateCssClass(tile, CSS_CLASS_TILE_EXPANDED);
			});
			current.play();
		}

		void cancelAndFallback(MouseEvent mouseEvent) {
			Duration time = newAnimation();

			double targetWidth = tile.getPrefHeight(); // let width be the same as height

			setTileStateCssClass(tile, CSS_CLASS_TILE_EXPANDING);

			current = new Timeline(new KeyFrame(time, new KeyValue(tile.prefWidthProperty(), targetWidth, interpolator)));
			current.setOnFinished(event -> {
				current = null;
				setTileStateCssClass(tile, CSS_CLASS_TILE_UNEXPANDED);
			});
			current.play();
		}

	}

	private void setTileStateCssClass(Tile tile, String cssClass) {
		List<String> cssClasses = tile.getStyleClass();
		cssClasses.remove(CSS_CLASS_TILE_UNEXPANDED);
		cssClasses.remove(CSS_CLASS_TILE_EXPANDING);
		cssClasses.remove(CSS_CLASS_TILE_EXPANDED);
		if (cssClass != null)
			cssClasses.add(cssClass);
	}

}
