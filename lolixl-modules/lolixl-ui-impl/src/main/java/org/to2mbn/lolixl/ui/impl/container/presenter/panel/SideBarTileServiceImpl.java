package org.to2mbn.lolixl.ui.impl.container.presenter.panel;

import javafx.application.Platform;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.to2mbn.lolixl.ui.SideBarTileService;
import org.to2mbn.lolixl.ui.model.SidebarTileElement;
import org.to2mbn.lolixl.utils.GsonUtils;
import org.to2mbn.lolixl.utils.ServiceUtils;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.lang.String.format;
import static java.util.stream.Collectors.toConcurrentMap;
import static org.to2mbn.lolixl.utils.FXUtils.checkFxThread;

@Service({ SideBarTileService.class })
@Component(immediate = true)
public class SideBarTileServiceImpl implements SideBarTileService {

	private static final Logger LOGGER = Logger.getLogger(SideBarTileServiceImpl.TileEntry.class.getCanonicalName());

	public static class TileEntry implements Serializable {

		private static final long serialVersionUID = 1L;

		String tagName;
		volatile SidebarTileElement tileElement;

		@Override
		public String toString() {
			return "[" + tagName + "]";
		}

	}

	public static class TileList implements Serializable {

		private static final long serialVersionUID = 1L;

		ArrayList<TileEntry> entries;
		volatile Map<String, TileEntry> tagNameMapping;

		@Override
		public String toString() {
			return String.valueOf(entries);
		}

	}

	TileList tiles;
	int maxShownTiles = 4; // TODO calculate by height

	Path tileListFile = Paths.get(".lolixl", "ui", "tiles-list.json");
	BundleContext bundleContext;
	ServiceTracker<SidebarTileElement, SidebarTileElement> serviceTracker;

	@Activate
	public void active(ComponentContext compCtx) {
		bundleContext = compCtx.getBundleContext();
		tryReadTilesListFile();
		serviceTracker = new ServiceTracker<>(bundleContext, SidebarTileElement.class, new ServiceTrackerCustomizer<SidebarTileElement, SidebarTileElement>() {

			@Override
			public SidebarTileElement addingService(ServiceReference<SidebarTileElement> reference) {
				SidebarTileElement service = bundleContext.getService(reference);
				Platform.runLater(() -> {
					String tagName = ServiceUtils.getIdProperty(SidebarTileElement.PROPERTY_TAG_NAME, reference, service);
					synchronized (tiles) {
						TileEntry entry = tiles.tagNameMapping.get(tagName);
						if (entry == null) {
							LOGGER.fine("Loading new tile: " + tagName);
							entry = new TileEntry();
							entry.tagName = tagName;
							entry.tileElement = service;
							tiles.tagNameMapping.put(tagName, entry);
							tiles.entries.add(entry);
						} else {
							LOGGER.fine("Loading old tile: " + tagName);
							entry.tileElement = service;
						}
					}
					saveTilesListFile();
					// TODO notify
				});
				return service;
			}

			@Override
			public void modifiedService(ServiceReference<SidebarTileElement> reference, SidebarTileElement service) {}

			@Override
			public void removedService(ServiceReference<SidebarTileElement> reference, SidebarTileElement service) {
				Platform.runLater(() -> {
					String tagName = ServiceUtils.getIdProperty(SidebarTileElement.PROPERTY_TAG_NAME, reference, service);
					TileEntry entry = tiles.tagNameMapping.get(tagName);
					if (entry == null) {
						LOGGER.warning(format("Tile service %s is going to be removed, but no tile entry for it is found"));
					} else {
						if (entry.tileElement == null) {
							LOGGER.warning(format("Tile service %s is going to be removed, but tileElement of its tile entry is null"));
						} else {
							entry.tileElement = null;
							LOGGER.fine("Removing tile service: " + tagName);
							// TODO notify
						}
					}
				});
				bundleContext.ungetService(reference);
			}
		});
	}

	void tryReadTilesListFile() {
		if (Files.isRegularFile(tileListFile)) {
			try {
				tiles = GsonUtils.fromJson(tileListFile, TileList.class);
				LOGGER.fine(() -> format("Loaded tiles list: %s", tiles));
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, format("Couldn't read tiles list [%s]", tileListFile), e);
			}
		}
		if (tiles == null) {
			tiles = new TileList();
		}
		if (tiles.entries == null) {
			tiles.entries = new ArrayList<>();
		}
		tiles.tagNameMapping = tiles.entries.stream()
				.collect(toConcurrentMap(entry -> entry.tagName, entry -> entry));
	}

	void saveTilesListFile() {
		TileList copy = new TileList();
		synchronized (tiles) {
			copy.entries = new ArrayList<>(tiles.entries);
		}
		try {
			synchronized (tileListFile) {
				GsonUtils.toJson(tileListFile, copy);
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, format("Couldn't tiles list to [%s]", tileListFile), e);
		}
	}

	@Override
	public SidebarTileElement[] getTiles(StackingStatus... types) {
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

		if (!includeShown && !includeHidden) {
			return new SidebarTileElement[0];
		}

		List<SidebarTileElement> result = new ArrayList<>();
		int size = 0;
		synchronized (tiles) {
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
		return result.toArray(new SidebarTileElement[size]);
	}

	@Override
	public StackingStatus getStatus(SidebarTileElement element) {
		checkFxThread();
		Objects.requireNonNull(element);

		int idx = 0;
		synchronized (tiles) {
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

		for (TileEntry ele : tiles.tagNameMapping.values()) {
			if (ele.tileElement == element) {
				return ele.tagName;
			}
		}
		return null;
	}

	@Override
	public int moveTile(SidebarTileElement element, int offset) {
		checkFxThread();
		Objects.requireNonNull(element);

		synchronized (tiles) {
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
			// TODO notify
			return idxDestDisplay - idxSrcDisplay;
		}
	}

}
