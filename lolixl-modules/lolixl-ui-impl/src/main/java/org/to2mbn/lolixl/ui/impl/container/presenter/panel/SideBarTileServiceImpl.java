package org.to2mbn.lolixl.ui.impl.container.presenter.panel;

import javafx.application.Platform;
import javafx.scene.layout.Region;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.to2mbn.lolixl.core.config.ConfigurationCategory;
import org.to2mbn.lolixl.ui.SideBarTileService;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.impl.container.presenter.panel.SideBarTileList.TileEntry;
import org.to2mbn.lolixl.ui.model.SidebarTileElement;
import org.to2mbn.lolixl.utils.ObservableContext;
import org.to2mbn.lolixl.utils.ServiceUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import static java.lang.String.format;
import static java.util.stream.Collectors.toConcurrentMap;
import static org.to2mbn.lolixl.utils.FXUtils.checkFxThread;

@Service({ SideBarTileService.class, ConfigurationCategory.class })
@Properties({
		@Property(name = ConfigurationCategory.PROPERTY_CATEGORY, value = SideBarTileService.CATEGORY_SIDEBAR_TILES)
})
@Component(immediate = true)
public class SideBarTileServiceImpl implements SideBarTileService, ConfigurationCategory<SideBarTileList> {

	private static final Logger LOGGER = Logger.getLogger(SideBarTileServiceImpl.class.getCanonicalName());

	private SideBarTileList tiles;
	private volatile int maxShownTiles;

	private ObservableContext observableContext;
	private BundleContext bundleContext;
	private ServiceTracker<SidebarTileElement, SidebarTileElement> serviceTracker;

	@Activate
	public void active(ComponentContext compCtx) {
		bundleContext = compCtx.getBundleContext();
		serviceTracker = new ServiceTracker<>(bundleContext, SidebarTileElement.class, new ServiceTrackerCustomizer<SidebarTileElement, SidebarTileElement>() {

			@Override
			public SidebarTileElement addingService(ServiceReference<SidebarTileElement> reference) {
				SidebarTileElement service = bundleContext.getService(reference);
				Platform.runLater(() -> {
					String tagName = ServiceUtils.getIdProperty(SidebarTileElement.PROPERTY_TAG_NAME, reference, service);
					synchronized (tiles.entries) {
						TileEntry entry = tiles.tagNameMapping.get(tagName);
						if (entry == null) {
							LOGGER.fine("Loading new tile: " + tagName);
							entry = new TileEntry();
							entry.tagName = tagName;
							tiles.tagNameMapping.put(tagName, entry);
							tiles.entries.add(entry);
						} else {
							LOGGER.fine("Loading old tile: " + tagName);
						}
						entry.tileElement = service;
						entry.tileComponent = service.createTile();
						tiles.serviceMapping.put(service, entry);
						tiles.componentMapping.put(entry.tileComponent, entry);
					}
					observableContext.notifyChanged();
				});
				return service;
			}

			@Override
			public void modifiedService(ServiceReference<SidebarTileElement> reference, SidebarTileElement service) {}

			@Override
			public void removedService(ServiceReference<SidebarTileElement> reference, SidebarTileElement service) {
				Platform.runLater(() -> {
					synchronized (tiles.entries) {
						TileEntry entry = tiles.serviceMapping.remove(service);
						if (entry == null) {
							LOGGER.warning(format("Tile service %s is going to be removed, but no tile entry for it is found"));
						} else {
							tiles.componentMapping.remove(entry.tileComponent);
							tiles.tagNameMapping.remove(entry.tagName);
							entry.tileComponent = null;
							entry.tileElement = null;
						}
					}
				});
				bundleContext.ungetService(reference);
			}
		});
	}

	@Deactivate
	public void deactive() {
		serviceTracker.close();
	}

	@Override
	public List<SidebarTileElement> getTiles(StackingStatus... types) {
		checkFxThread();
		Objects.requireNonNull(types);

		boolean includeShown = false;
		boolean includeHidden = false;
		for (StackingStatus type : types) {
			switch (type) {
				case HIDDEN:
					includeHidden = true;
					break;
				case SHOWN:
					includeShown = true;
					break;
				default:
					break;
			}
		}

		List<SidebarTileElement> result = new ArrayList<>();
		if (!includeShown && !includeHidden) {
			return result;
		}

		int size = 0;
		synchronized (tiles.entries) {
			for (TileEntry ele : tiles.entries) {
				if (ele.tileElement != null) {
					if (size < maxShownTiles) {
						if (includeShown) {
							result.add(ele.tileElement);
						}
					} else {
						if (includeHidden) {
							result.add(ele.tileElement);
						}
					}
					size++;
				}
			}
		}
		return result;
	}

	@Override
	public StackingStatus getStatus(SidebarTileElement element) {
		checkFxThread();
		Objects.requireNonNull(element);

		int idx = 0;
		synchronized (tiles.entries) {
			for (TileEntry ele : tiles.entries) {
				if (ele.tileElement != null) {
					if (ele.tileElement == element) {
						return idx < maxShownTiles ? StackingStatus.SHOWN : StackingStatus.HIDDEN;
					}
					idx++;
				}
			}
		}

		return null;

	}

	@Override
	public String getTagName(SidebarTileElement element) {
		Objects.requireNonNull(element);

		TileEntry entry = tiles.serviceMapping.get(element);
		if (entry != null) {
			return entry.tagName;
		}
		return null;
	}

	@Override
	public Tile getTileComponent(SidebarTileElement element) {
		Objects.requireNonNull(element);

		TileEntry entry = tiles.serviceMapping.get(element);
		if (entry != null) {
			return entry.tileComponent;
		}
		return null;
	}

	@Override
	public SidebarTileElement getTileByComponent(Tile component) {
		Objects.requireNonNull(component);

		TileEntry entry = tiles.componentMapping.get(component);
		if (entry != null) {
			return entry.tileElement;
		}
		return null;
	}

	@Override
	public int moveTile(SidebarTileElement element, int offset) {
		checkFxThread();
		Objects.requireNonNull(element);

		int result;

		synchronized (tiles.entries) {
			List<TileEntry> entries = tiles.entries;
			int idxSrc = entries.indexOf(element);
			if (idxSrc == -1 || offset == 0) {
				return 0;
			}
			int idxSrcDisplay = 0;
			for (int i = 0; i < entries.size(); i++) {
				SidebarTileElement tileEle = entries.get(i).tileElement;
				if (tileEle == element) {
					break;
				} else if (tileEle != null) {
					idxSrcDisplay++;
				}
			}
			int idxDestDisplay = idxSrc + offset;
			int idxDest = -1;
			if (idxDestDisplay <= 0) {
				idxDest = 0;
			} else {
				int iDisplay = 0;

				for (int i = 0; i < entries.size(); i++) {
					if (entries.get(i).tileElement != null) {
						if (iDisplay == idxDestDisplay) {
							idxDest = i;
							break;
						}
						iDisplay++;
					}
				}
				if (idxDest == -1) {
					idxDest = entries.size();
					idxDestDisplay = iDisplay;
				}
			}
			if (idxDest == idxSrc) {
				return 0;
			}
			TileEntry entry = entries.get(idxSrc);
			entries.remove(idxSrc);
			if (idxDest > idxSrc) {
				entries.add(idxDest - 1, entry);
			} else {
				entries.add(idxDest, entry);
			}
			result = idxDestDisplay - idxSrcDisplay;
		}
		observableContext.notifyChanged();
		return result;
	}

	@Override
	public void setMaxShownTiles(int newMaxShownTiles) {
		if (maxShownTiles != newMaxShownTiles) {
			maxShownTiles = newMaxShownTiles;
			observableContext.notifyChanged();
		}
	}

	@Override
	public void setObservableContext(ObservableContext ctx) {
		this.observableContext = ctx;
	}

	@Override
	public SideBarTileList store() {
		return tiles;
	}

	@Override
	public void restore(SideBarTileList memento) {
		tiles = memento;

		if (tiles == null) {
			tiles = new SideBarTileList();
		}
		if (tiles.entries == null) {
			tiles.entries = new Vector<>();
		}
		tiles.tagNameMapping = tiles.entries.stream()
				.collect(toConcurrentMap(entry -> entry.tagName, entry -> entry));
		tiles.serviceMapping = new ConcurrentHashMap<>();
		tiles.componentMapping = new ConcurrentHashMap<>();

		serviceTracker.open(true);
	}

	@Override
	public Class<? extends SideBarTileList> getMementoType() {
		return SideBarTileList.class;
	}

	@Override
	public String getLocalizedName() {
		return null;
	}

	@Override
	public Region createConfiguringPanel() {
		return null;
	}

}
