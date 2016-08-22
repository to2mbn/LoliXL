package org.to2mbn.lolixl.ui.impl.container.presenter;

import java.util.List;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.ui.Panel;
import org.to2mbn.lolixl.ui.SideBarPanelDisplayService;
import org.to2mbn.lolixl.ui.SideBarTileService;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.view.HomeContentView;
import org.to2mbn.lolixl.ui.model.SidebarTileElement;
import org.to2mbn.lolixl.utils.FunctionInterpolator;
import org.to2mbn.lolixl.utils.MappedObservableList;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.util.Duration;

@Service({ HomeContentPresenter.class })
@Component(immediate = true)
public class HomeContentPresenter extends Presenter<HomeContentView> {

	private static final String FXML_LOCATION = "fxml/org.to2mbn.lolixl.ui.home/content.fxml";

	private static final String CSS_CLASS_TILE = "xl-sidebar-tile";
	private static final String CSS_CLASS_TILE_EXPANDED = "xl-sidebar-tile-expanded";
	private static final String CSS_CLASS_TILE_UNEXPANDED = "xl-sidebar-tile-unexpanded";
	private static final String CSS_CLASS_TILE_EXPANDING = "xl-sidebar-tile-expanding";

	private double tileAnimationDuration = 300.0;
	private double tileHeight = 60.0;

	@Reference
	private SideBarTileService tileService;

	private IntegerBinding shownTilesCount;

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
		view.startGameButton.textProperty().bind(I18N.localize("org.to2mbn.lolixl.ui.home.launch_button.text"));

		shownTilesCount = Bindings.createIntegerBinding(() -> {
			Insets padding = view.tileContainer.getInsets();
			double nodeHeight = view.tileContainer.getHeight();
			double tileSpacing = view.tileContainer.getSpacing();
			double effectiveHeight = nodeHeight - padding.getBottom() - padding.getTop();
			int capacity = (int) ((effectiveHeight + tileSpacing) / (tileHeight + tileSpacing));
			int shownCount = Math.max(capacity - 1, 0);
			return shownCount;
		}, view.tileContainer.heightProperty());
		tileService.maxShownTilesProperty().bind(shownTilesCount);

		// XXX: TEST CODE, delete it
		view.startGameButton.setOnAction(event -> {
			BundleContext ctx = FrameworkUtil.getBundle(getClass()).getBundleContext();
			SideBarPanelDisplayService display = ctx.getService(ctx.getServiceReference(SideBarPanelDisplayService.class));
			Platform.runLater(() -> {
				Panel panel = display.newPanel();
				SidebarTileElement service = ctx.getService(ctx.getServiceReference(SidebarTileElement.class));
				panel.bindItem(service);
				panel.show();
			});
		});
		//
	}

	/**
	 * Lazy-bind for tileManagementTile.
	 *
	 * @param tile
	 */
	public void setManagementTile(Tile tile) {
		tile.getStyleClass().add("xl-sidebar-tile-management");
		view.tileRootContainer.setBottom(tile);
		BorderPane.setAlignment(tile, Pos.CENTER_RIGHT);
	}

	private void resolveTile(Tile tile) {
		TileAnimationHandler animationHandler = new TileAnimationHandler(tile);
		tile.addEventHandler(MouseEvent.MOUSE_ENTERED, animationHandler::mouseEntered);
		tile.addEventHandler(MouseEvent.MOUSE_EXITED, animationHandler::mouseExited);
		tile.addEventHandler(MouseEvent.MOUSE_PRESSED, animationHandler::mousePressedOrReleased);
		tile.addEventHandler(MouseEvent.MOUSE_RELEASED, animationHandler::mousePressedOrReleased);
		tile.getStyleClass().add(CSS_CLASS_TILE);
		setTileStateCssClass(tile, CSS_CLASS_TILE_UNEXPANDED);
		tile.maxWidthProperty().set(Region.USE_PREF_SIZE);
		tile.maxHeightProperty().set(Region.USE_PREF_SIZE);
		tile.minWidthProperty().set(Region.USE_PREF_SIZE);
		tile.minHeightProperty().set(Region.USE_PREF_SIZE);
	}

	private class TileAnimationHandler {

		static final int UNEXPANDED = 0;
		static final int EXPANDED = 1;

		int state = UNEXPANDED;
		boolean mouseIn = false;
		Tile tile;
		Timeline current;

		TileAnimationHandler(Tile tile) {
			this.tile = tile;
		}

		void mouseEntered(MouseEvent mouseEvent) {
			mouseIn = true;
			updateAnimation(mouseEvent);
		}

		void mouseExited(MouseEvent mouseEvent) {
			mouseIn = false;
			updateAnimation(mouseEvent);
		}

		void mousePressedOrReleased(MouseEvent mouseEvent) {
			updateAnimation(mouseEvent);
		}

		void updateAnimation(MouseEvent mouseEvent) {
			if (tile.isPressed()) {
				return;
			}
			if (mouseIn && state != EXPANDED) {
				runRollOutAnimation(mouseEvent);
			} else if (!mouseIn && state != UNEXPANDED) {
				cancelAndFallback(mouseEvent);
			}
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
			double originPrefWidth = tile.prefWidthProperty().get();
			Insets originPadding = tile.paddingProperty().get();

			setTileStateCssClass(tile, null);
			tile.applyCss();

			tile.prefWidthProperty().set(-1);
			tile.contentDisplayProperty().set(ContentDisplay.LEFT);

			double targetWidth = tile.prefWidth(-1);

			tile.prefWidthProperty().set(originPrefWidth);
			tile.paddingProperty().set(originPadding);

			// We don't restore contentDisplay in order to fix that
			// JFX doesn't apply css on contentDisplay until the animation finished.

			// ===

			state = EXPANDED;
			setTileStateCssClass(tile, CSS_CLASS_TILE_EXPANDING);

			current = new Timeline(new KeyFrame(time, new KeyValue(tile.prefWidthProperty(), targetWidth, FunctionInterpolator.S_CURVE)));
			current.setOnFinished(event -> {
				current = null;
				setTileStateCssClass(tile, CSS_CLASS_TILE_EXPANDED);
			});
			current.play();
		}

		void cancelAndFallback(MouseEvent mouseEvent) {
			Duration time = newAnimation();

			double targetWidth = tile.getPrefHeight(); // let width be the same as height

			state = UNEXPANDED;
			setTileStateCssClass(tile, CSS_CLASS_TILE_EXPANDING);

			current = new Timeline(new KeyFrame(time, new KeyValue(tile.prefWidthProperty(), targetWidth, FunctionInterpolator.S_CURVE)));
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
