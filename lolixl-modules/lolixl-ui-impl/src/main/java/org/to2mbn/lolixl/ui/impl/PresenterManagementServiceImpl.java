package org.to2mbn.lolixl.ui.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.to2mbn.lolixl.ui.PresenterManagementService;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.event.UIInitializationEvent;
import org.to2mbn.lolixl.ui.event.UIPostInitializationEvent;
import org.to2mbn.lolixl.ui.impl.container.presenter.DefaultFramePresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.DefaultSideBarPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.DefaultTitleBarPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.HomeContentPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panel.downloads.DownloadCenterPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panel.settings.SettingsPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panel.settings.ThemesPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panel.sidebar.GameVersionsPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panel.tiles.HiddenTilesPresenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.panel.tiles.TileManagingPresenter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Logger;

@Service({ PresenterManagementService.class, EventHandler.class })
@Properties({
	@Property(name = EventConstants.EVENT_TOPIC, value = { UIInitializationEvent.TOPIC_INITIALIZE, UIPostInitializationEvent.TOPIC_POST_INITIALIZE })
})
@Component(immediate = true)
public class PresenterManagementServiceImpl implements PresenterManagementService {
	private static final Logger LOGGER = Logger.getLogger(PresenterManagementServiceImpl.class.getCanonicalName());
	private final Deque<Presenter> presenters = new ConcurrentLinkedDeque<>();

	@Activate
	public void active(ComponentContext componentCtx) {
		BundleContext ctx = componentCtx.getBundleContext();
		initializePresenters(ctx);
	}

	@Override
	public <T extends Presenter> T getPresenter(Class<T> type) {
		for (Iterator<Presenter> iterator = presenters.iterator(); iterator.hasNext(); ) {
			Presenter next = iterator.next();
			if (next.getClass().isAssignableFrom(type)) {
				return (T) next;
			}
		}
		return null;
	}

	@Override
	public void handleEvent(Event event) {
		if (event instanceof UIInitializationEvent) {
			initializeViews();
		} else {
			postInitializePresenters();
		}
	}

	private void initializePresenters(BundleContext ctx) {
		presenters.addLast(new DefaultFramePresenter(ctx));
		presenters.addLast(new DefaultTitleBarPresenter(ctx));
		presenters.addLast(new DefaultSideBarPresenter(ctx));
		presenters.addLast(new TileManagingPresenter(ctx));
		presenters.addLast(new HiddenTilesPresenter(ctx));
		presenters.addLast(new SettingsPresenter(ctx));
		presenters.addLast(new GameVersionsPresenter(ctx));
		presenters.addLast(new ThemesPresenter(ctx));
		presenters.addLast(new DownloadCenterPresenter(ctx));
		presenters.addLast(new HomeContentPresenter(ctx));
	}

	private void initializeViews() {
		LOGGER.info("Initializing views of presenters");
		presenters.forEach(presenter -> {
			try {
				presenter.initializeView();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	private void postInitializePresenters() {
		LOGGER.info("Post-initializing presenters");
		presenters.forEach(presenter -> presenter.postInitialize());
	}
}
