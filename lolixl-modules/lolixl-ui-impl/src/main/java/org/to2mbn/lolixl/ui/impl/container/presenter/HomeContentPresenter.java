package org.to2mbn.lolixl.ui.impl.container.presenter;

import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.collections.ListChangeListener;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.to2mbn.lolixl.core.config.ConfigurationEvent;
import org.to2mbn.lolixl.ui.SideBarTileService;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.view.HomeContentView;
import org.to2mbn.lolixl.ui.model.SidebarTileElement;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service({ HomeContentPresenter.class, EventHandler.class })
@Properties({
		@Property(name = EventConstants.EVENT_TOPIC, value = ConfigurationEvent.TOPIC_CONFIGURATION),
		@Property(name = EventConstants.EVENT_FILTER, value = "(" + ConfigurationEvent.KEY_CATEGORY + "=" + SideBarTileService.CATEGORY_SIDEBAR_TILES + ")")
})
@Component(immediate = true)
public class HomeContentPresenter extends Presenter<HomeContentView> implements EventHandler {

	private static final String FXML_LOCATION = "/ui/fxml/container/home_content.fxml";

	@Reference
	private SideBarTileService tileService;

	@Activate
	public void active(ComponentContext compCtx) {
		super.active();
	}

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
	}

	/**
	 * Lazy-bind for tileManagementTile.
	 *
	 * @param tile
	 */
	public void setManagementTile(Tile tile) {
		view.tileRootContainer.setBottom(tile);
	}

	@Override
	public void handleEvent(Event event) {
		refreshShownTiles();
	}

	private void refreshShownTiles() {
		// FIXME: 简直日了狗 在idea下用lambda会报错
		tileService.getTiles(SideBarTileService.StackingStatus.SHOWN).addListener(new ListChangeListener<SidebarTileElement>() {
			@Override
			public void onChanged(Change<? extends SidebarTileElement> change) {
				change.getAddedSubList().forEach(ele -> {
					Tile tile = ele.createTile();
					tile.addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
					});
					tile.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
					});
				});
			}
		});
	}

	private static class SideBarTileAnimationHandler {
		private final Tile tile;
		private final AtomicBoolean isPlaying = new AtomicBoolean(false);
		private final AtomicReference<Transition> currentAnimation = new AtomicReference<>(null);

		private SideBarTileAnimationHandler(Tile _tile) {
			tile = _tile;
		}

		private void runRollOutAnimation() {
			TranslateTransition tran = new TranslateTransition(Duration.millis(800), tile);
			double x = tile.getLayoutX();
			tran.setFromX(x + tile.getWidth());
			tran.setToX(x);
			tran.setOnFinished(event -> isPlaying.set(false));
			tran.play();
		}

		private void cancelAndFallback() {
			// TODO
		}
	}
}
