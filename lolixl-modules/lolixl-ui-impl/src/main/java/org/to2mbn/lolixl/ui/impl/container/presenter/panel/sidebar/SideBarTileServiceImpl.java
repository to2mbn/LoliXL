package org.to2mbn.lolixl.ui.impl.container.presenter.panel.sidebar;

import static java.lang.String.format;
import static java.util.stream.Collectors.toConcurrentMap;
import static org.to2mbn.lolixl.utils.FXUtils.checkFxThread;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.core.config.ConfigurationCategory;
import org.to2mbn.lolixl.ui.SideBarTileService;
import org.to2mbn.lolixl.ui.model.SidebarTileElement;
import org.to2mbn.lolixl.utils.LambdaServiceTracker;
import org.to2mbn.lolixl.utils.LinkedObservableList;
import org.to2mbn.lolixl.utils.ObservableContext;
import org.to2mbn.lolixl.utils.ServiceUtils;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

@Service({ SideBarTileService.class, ConfigurationCategory.class })
@Properties({
		@Property(name = ConfigurationCategory.PROPERTY_CATEGORY, value = SideBarTileServiceImpl.CATEGORY_SIDEBAR_TILES)
})
@Component(immediate = true)
public class SideBarTileServiceImpl implements SideBarTileService, ConfigurationCategory<SideBarTileList> {

	public static final String CATEGORY_SIDEBAR_TILES = "org.to2mbn.lolixl.ui.sideBarTiles";

	private static final Logger LOGGER = Logger.getLogger(SideBarTileServiceImpl.class.getCanonicalName());

	private SideBarTileList tiles = new SideBarTileList();
	private IntegerProperty maxShownTilesProperty = new SimpleIntegerProperty();

	private ObservableList<SidebarTileElement> shownTiles = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
	private ObservableList<SidebarTileElement> hiddenTiles = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
	private ObservableList<SidebarTileElement> allTilesReadOnlyView = new LinkedObservableList<>(shownTiles, hiddenTiles);
	private ObservableList<SidebarTileElement> shownTilesReadOnlyView = FXCollections.unmodifiableObservableList(shownTiles);
	private ObservableList<SidebarTileElement> hiddenTilesReadOnlyView = FXCollections.unmodifiableObservableList(hiddenTiles);

	private ObservableContext observableContext;
	private BundleContext bundleContext;
	private LambdaServiceTracker<SidebarTileElement> serviceTracker;

	@Activate
	public void active(ComponentContext compCtx) {
		bundleContext = compCtx.getBundleContext();
		maxShownTilesProperty.addListener((dummy, oldV, newV) -> {
			if (oldV != newV) {
				updateTiles();
			}
		});
		serviceTracker = new LambdaServiceTracker<>(bundleContext, SidebarTileElement.class)
				.whenAdding((reference, service) -> {
					String tagName = ServiceUtils.getIdProperty(SidebarTileElement.PROPERTY_TAG_NAME, reference, service);
					synchronized (tiles.entries) {
						SideBarTileList.TileEntry entry = tiles.tagNameMapping.get(tagName);
						if (entry == null) {
							LOGGER.fine("Loading new tile: " + tagName);
							entry = new SideBarTileList.TileEntry();
							entry.tagName = tagName;
							tiles.tagNameMapping.put(tagName, entry);
							tiles.entries.add(entry);
						} else {
							LOGGER.fine("Loading old tile: " + tagName);
						}
						entry.tileElement = service;
						tiles.serviceMapping.put(service, entry);
						updateTiles();
					}
					observableContext.notifyChanged();
				})
				.whenRemoving((reference, service) -> {
					synchronized (tiles.entries) {
						SideBarTileList.TileEntry entry = tiles.serviceMapping.remove(service);
						if (entry == null) {
							LOGGER.warning(format("Tile service %s is going to be removed, but no tile entry for it is found"));
						} else {
							tiles.tagNameMapping.remove(entry.tagName);
							entry.tileElement = null;
						}
						updateTiles();
					}
				});
	}

	@Deactivate
	public void deactive() {
		serviceTracker.close();
	}

	@Override
	public ObservableList<SidebarTileElement> getTiles(StackingStatus... types) {
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
			return FXCollections.emptyObservableList();
		} else if (includeShown && !includeHidden) {
			return shownTilesReadOnlyView;
		} else if (!includeShown && includeHidden) {
			return hiddenTilesReadOnlyView;
		} else if (includeShown && includeHidden) {
			return allTilesReadOnlyView;
		}
		throw new AssertionError("unreachable statement");
	}

	private void updateTiles() {
		List<SidebarTileElement> shown = new ArrayList<>();
		List<SidebarTileElement> hidden = new ArrayList<>();

		int size = 0;
		for (SideBarTileList.TileEntry ele : tiles.entries) {
			if (ele.tileElement != null) {
				if (size < maxShownTilesProperty.get()) {
					shown.add(ele.tileElement);
				} else {
					hidden.add(ele.tileElement);
				}
				size++;
			}
		}
		shownTiles.setAll(shown);
		hiddenTiles.setAll(hidden);
	}

	@Override
	public StackingStatus getStatus(SidebarTileElement element) {
		checkFxThread();
		Objects.requireNonNull(element);

		int idx = 0;
		synchronized (tiles.entries) {
			for (SideBarTileList.TileEntry ele : tiles.entries) {
				if (ele.tileElement != null) {
					if (ele.tileElement == element) {
						return idx < maxShownTilesProperty.get() ? StackingStatus.SHOWN : StackingStatus.HIDDEN;
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

		SideBarTileList.TileEntry entry = tiles.serviceMapping.get(element);
		if (entry != null) {
			return entry.tagName;
		}
		return null;
	}

	@Override
	public int moveTile(SidebarTileElement element, int offset) {
		checkFxThread();
		Objects.requireNonNull(element);

		int result;

		synchronized (tiles.entries) {
			List<SideBarTileList.TileEntry> entries = tiles.entries;
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
			SideBarTileList.TileEntry entry = entries.get(idxSrc);
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
	public void setObservableContext(ObservableContext ctx) {
		this.observableContext = ctx;
	}

	@Override
	public SideBarTileList store() {
		return tiles;
	}

	@Override
	public void restore(Optional<SideBarTileList> optionalMemento) {
		optionalMemento.ifPresent(memento -> {
			tiles.entries.addAll(memento.entries);
			tiles.tagNameMapping = tiles.entries.stream()
					.collect(toConcurrentMap(entry -> entry.tagName, entry -> entry));
			tiles.serviceMapping = new ConcurrentHashMap<>();
		});

		serviceTracker.open(true);
	}

	@Override
	public Class<? extends SideBarTileList> getMementoType() {
		return SideBarTileList.class;
	}

	@Override
	public IntegerProperty maxShownTilesProperty() {
		return maxShownTilesProperty;
	}

}
