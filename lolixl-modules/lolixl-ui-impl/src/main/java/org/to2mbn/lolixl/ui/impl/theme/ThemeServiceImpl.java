package org.to2mbn.lolixl.ui.impl.theme;

import static java.util.stream.Collectors.toList;
import java.util.ArrayList;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.to2mbn.lolixl.core.config.ConfigurationCategory;
import org.to2mbn.lolixl.ui.impl.MainScene;
import org.to2mbn.lolixl.ui.impl.theme.ThemeConfiguration.ThemeEntry;
import org.to2mbn.lolixl.ui.theme.Theme;
import org.to2mbn.lolixl.ui.theme.ThemeService;
import org.to2mbn.lolixl.utils.CollectionUtils;
import org.to2mbn.lolixl.utils.LinkedObservableList;
import org.to2mbn.lolixl.utils.ObservableContext;
import org.to2mbn.lolixl.utils.ServiceUtils;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * <pre>
 * 我走过山时，山不说话，
 * 我路过海时，海不说话，
 * 小键盘滴滴答答，伴我走天涯。
 * 大家都说我因为爱着MC，才在Java出了家，
 * 其实我只是爱上了Java的云和霞，像极了十三岁那年的烟花。
 * </pre>
 * 
 * @author yushijinhun
 */
@Service({ ThemeService.class, ConfigurationCategory.class })
@Properties({
		@Property(name = ConfigurationCategory.PROPERTY_CATEGORY, value = ThemeServiceImpl.CATEGORY_THEME_CONFIG)
})
@Component(immediate = true)
public class ThemeServiceImpl implements ThemeService, ConfigurationCategory<ThemeConfiguration> {

	public static final String CATEGORY_THEME_CONFIG = "org.to2mbn.lolixl.ui.impl.theme.config";

	private static final Logger LOGGER = Logger.getLogger(ThemeServiceImpl.class.getCanonicalName());

	@Reference(target = "(" + MainScene.PROPERTY_SCENE_ID + "=" + MainScene.MAIN_SCENE_ID + ")")
	private Scene scene;

	private Map<String, Integer> themeTypeOrder;
	private Comparator<ThemeEntry> themeComparator;

	private ServiceTracker<Theme, Theme> serviceTracker;
	private BundleContext bundleContext;
	private ObservableContext observableContext;

	private ThemeConfiguration config = new ThemeConfiguration();

	private ObservableList<Theme> enabledThemes = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
	private ObservableList<Theme> disabledThemes = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
	private ObservableList<Theme> allTilesReadOnlyView = new LinkedObservableList<>(enabledThemes, disabledThemes);
	private ObservableList<Theme> enabledThemesReadOnlyView = FXCollections.unmodifiableObservableList(enabledThemes);
	private ObservableList<Theme> disabledThemesReadOnlyView = FXCollections.unmodifiableObservableList(disabledThemes);

	public ThemeServiceImpl() {
		themeTypeOrder = new HashMap<>();
		themeTypeOrder.put(Theme.TYPE_COLOR, 1);
		themeTypeOrder.put(Theme.TYPE_FONT, 2);
		themeTypeOrder.put(Theme.TYPE_THEME_PACKAGE, 3);
		themeComparator = Comparator.comparingInt((ThemeEntry entry) -> getThemeTypeOrder(entry.serviceRef))
				.thenComparing(Comparator.comparing((ThemeEntry entry) -> entry.serviceRef).reversed());
	}

	private int getThemeTypeOrder(ServiceReference<Theme> ref) {
		String type = getThemeType(ref);
		if (type != null) {
			Integer order = themeTypeOrder.get(type);
			if (order != null) {
				return order;
			}
		}
		return 0;
	}

	@Activate
	public void active(ComponentContext compCtx) {
		this.bundleContext = compCtx.getBundleContext();
		serviceTracker = new ServiceTracker<>(bundleContext, Theme.class, new ServiceTrackerCustomizer<Theme, Theme>() {

			@Override
			public Theme addingService(ServiceReference<Theme> reference) {
				Theme service = bundleContext.getService(reference);
				String themeId = ServiceUtils.getIdProperty(Theme.PROPERTY_THEME_ID, reference, service);
				synchronized (config.themes) {
					ThemeEntry themeEntry = null;
					boolean newAdd = false;
					for (ThemeEntry ele : config.themes) {
						if (themeId.equals(ele.id)) {
							ele.serviceRef = reference;
							ele.theme = service;
							themeEntry = ele;
							break;
						}
					}
					if (themeEntry == null) {
						newAdd = true;
						themeEntry = new ThemeEntry();
						themeEntry.id = themeId;
						themeEntry.serviceRef = reference;
						themeEntry.theme = service;
						config.themes.add(themeEntry);
					}
					if (newAdd) {
						// 过去没有设置过这个Theme

						List<ThemeEntry> availableThemes = filterAvailableThemes(getThemeType(reference));
						long enabledCount = countEnabledThemes(availableThemes);
						if (enabledCount == 0) {
							// 所有负责该职责的Theme都被禁用
							// 那么将当前这个Theme启用
							themeEntry.enabled = true;

						} else if (enabledCount == 1) {
							// 有一个启用的Theme
							if (themeEntry.enabled) {
								// 启用的Theme为当前的Theme
								// 这是不可能的
								throw new IllegalStateException("The new-added theme cannot be enabled");
							} else {
								// 为其它Theme
								ThemeEntry enabledTheme = availableThemes.stream()
										.filter(entry -> entry.enabled)
										.findFirst().get();
								// 对比优先级
								if (themeComparator.compare(enabledTheme, themeEntry) > 0) {
									// enabledTheme > themeEntry
									// 不改变
								} else {
									// enabledTheme <= themeEntry
									// 选用当前Theme
									enabledTheme.enabled = false;
									themeEntry.enabled = true;
								}
							}
						} else {
							// enabledCount > 1
							// 这TM该咋办...fuck......
							// 我看还是把当前的Theme关了好
						}
					} else {
						// 过去设置过这个Theme，遵循过去的设置
					}
					updateThemesList();
				}
				return service;
			}

			@Override
			public void modifiedService(ServiceReference<Theme> reference, Theme service) {}

			@Override
			public void removedService(ServiceReference<Theme> reference, Theme service) {
				synchronized (config.themes) {
					for (ThemeEntry entry : config.themes) {
						if (entry.theme == service) {
							entry.serviceRef = null;
							entry.theme = null;
							if (entry.enabled) {
								String type = getThemeType(reference);
								List<ThemeEntry> availableThemes = filterAvailableThemes(type);
								long enabledCount = countEnabledThemes(availableThemes);
								if (enabledCount == 0) {
									// 得找个后继
									if (Theme.TYPE_THEME_PACKAGE.equals(type)) {
										// 当然只有在必须的时候才这样
										if (availableThemes.size() > 0) {
											availableThemes.get(0).enabled = true;
										} else {
											// 没救了
											LOGGER.warning("All themes for theme-package have been removed");
										}
									}
								}
							}
							updateThemesList();
							break;
						}
					}
				}
				bundleContext.ungetService(reference);
			}
		});
	}

	private List<ThemeEntry> filterAvailableThemes(String themeType) {
		return config.themes.stream()
				.filter(entry -> entry.theme != null)
				.filter(entry -> Objects.equals(themeType, getThemeType(entry.serviceRef)))
				.sorted(themeComparator)
				.collect(toList());
	}

	private long countEnabledThemes(Collection<ThemeEntry> themes) {
		return themes.stream()
				.filter(entry -> entry.enabled)
				.count();
	}

	private String getThemeType(ServiceReference<Theme> ref) {
		return (String) ref.getProperty(Theme.PROPERTY_THEME_TYPE);
	}

	@Deactivate
	public void deactive() {
		serviceTracker.close();
	}

	@Override
	public void enable(Theme theme) {
		setEnable(theme, true);
	}

	@Override
	public void disable(Theme theme) {
		setEnable(theme, false);
	}

	private void setEnable(Theme theme, boolean enable) {
		Objects.requireNonNull(theme);
		synchronized (config.themes) {
			for (ThemeEntry entry : config.themes) {
				if (entry.theme == theme && entry.enabled != enable) {
					LOGGER.fine((enable ? "Enabling" : "Disabling") + " theme " + entry.id);
					entry.enabled = enable;
					updateThemesList();
				}
			}
		}
	}

	private void updateThemesList() {
		List<Theme> newEnabled = new ArrayList<>();
		List<Theme> newDisabled = new ArrayList<>();
		config.themes.stream()
				.filter(entry -> entry.theme != null)
				.sorted()
				.forEach(entry -> (entry.enabled ? newEnabled : newDisabled).add(entry.theme));
		enabledThemes.setAll(newEnabled);
		disabledThemes.setAll(newDisabled);

		processThemesUpdate();
		observableContext.notifyChanged();
	}

	@Override
	public ObservableList<Theme> getEnabledThemes() {
		return enabledThemesReadOnlyView;
	}

	@Override
	public ObservableList<Theme> getDisabledThemes() {
		return disabledThemesReadOnlyView;
	}

	@Override
	public ObservableList<Theme> getAllThemes() {
		return allTilesReadOnlyView;
	}

	@Override
	public void setObservableContext(ObservableContext ctx) {
		this.observableContext = ctx;
	}

	@Override
	public ThemeConfiguration store() {
		return config;
	}

	@Override
	public void restore(Optional<ThemeConfiguration> optionalMemento) {
		optionalMemento.ifPresent(memento -> config.themes.addAll(memento.themes));
		serviceTracker.open(true);
	}

	@Override
	public Class<? extends ThemeConfiguration> getMementoType() {
		return ThemeConfiguration.class;
	}

	@Override
	public ObservableStringValue getLocalizedName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Region createConfiguringPanel() {
		// TODO Auto-generated method stub
		return null;
	}

	private List<Theme> lastEnabledThemes = new ArrayList<>();

	private void processThemesUpdate() {
		CollectionUtils.diff(lastEnabledThemes, enabledThemes,
				installed -> {
					LOGGER.fine("Loading theme " + installed);
					ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
					Thread.currentThread().setContextClassLoader(installed.getResourceLoader());
					scene.getStylesheets().addAll(installed.getStyleSheets());
					Thread.currentThread().setContextClassLoader(ctxLoader);
				},
				uninstalled -> {
					LOGGER.fine("Unloading theme " + uninstalled);
					scene.getStylesheets().removeAll(uninstalled.getStyleSheets());
				});
		lastEnabledThemes.clear();
		lastEnabledThemes.addAll(enabledThemes);
	}

}
