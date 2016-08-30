package org.to2mbn.lolixl.ui.impl.theme.background;

import static org.to2mbn.lolixl.utils.FXUtils.checkFxThread;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.core.config.ConfigurationCategory;
import org.to2mbn.lolixl.ui.theme.background.BackgroundProvider;
import org.to2mbn.lolixl.ui.theme.background.BackgroundService;
import org.to2mbn.lolixl.utils.ObservableContext;
import org.to2mbn.lolixl.utils.ObservableServiceTracker;
import org.to2mbn.lolixl.utils.ServiceUtils;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.ObservableList;
import javafx.scene.layout.Background;

@Service({ BackgroundService.class, ConfigurationCategory.class })
@Properties({
		@Property(name = ConfigurationCategory.PROPERTY_CATEGORY, value = "org.to2mbn.lolixl.ui.theme.background")
})
@Component(immediate = true)
public class BackgroundServiceImpl implements BackgroundService, ConfigurationCategory<BackgroundConfig> {

	private ObservableServiceTracker<BackgroundProvider> serviceTracker;
	private Map<String, BackgroundProvider> id2background = new ConcurrentHashMap<>();

	private StringProperty selectedId = new SimpleStringProperty();

	private ObjectBinding<BackgroundProvider> currentBackgroundProvider;
	private ObjectBinding<Background> currentBackground;
	private InvalidationListener backgroundObserver;
	private InvalidationListener weakBackgroundObserver;

	@Activate
	public void active(ComponentContext compCtx) {
		serviceTracker = new ObservableServiceTracker<>(compCtx.getBundleContext(), BackgroundProvider.class);
		serviceTracker
				.whenAdding((ref, service) -> id2background.put(ServiceUtils.getIdProperty(BackgroundProvider.PROPERTY_BACKGROUND_ID, ref, service), service))
				.whenRemoving((ref, service) -> id2background.remove(ServiceUtils.getIdProperty(BackgroundProvider.PROPERTY_BACKGROUND_ID, ref, service)));
		serviceTracker.open(true);

		currentBackgroundProvider = new ObjectBinding<BackgroundProvider>() {

			{
				bind(selectedId, getBackgroundProviders());
			}

			@Override
			protected BackgroundProvider computeValue() {
				String l_selectedId = selectedId.get();
				if (l_selectedId != null) {
					BackgroundProvider selected = id2background.get(l_selectedId);
					if (selected != null) {
						return selected;
					}
				}
				if (getBackgroundProviders().size() > 0) {
					return getBackgroundProviders().get(0);
				}
				return null;
			}
		};

		currentBackground = new ObjectBinding<Background>() {

			{
				bind(currentBackgroundProvider);
			}

			@Override
			protected Background computeValue() {
				BackgroundProvider provider = currentBackgroundProvider.get();
				if (provider == null) {
					return null;
				} else {
					return provider.getBackground().get();
				}
			}
		};

		backgroundObserver = observable -> currentBackground.invalidate();
		weakBackgroundObserver = new WeakInvalidationListener(backgroundObserver);

		currentBackgroundProvider.addListener((observable, oldVal, newVal) -> {
			if (oldVal != null) {
				oldVal.getBackground().removeListener(weakBackgroundObserver);
			}
			if (newVal != null) {
				newVal.getBackground().addListener(weakBackgroundObserver);
			}
		});
	}

	@Deactivate
	public void deactive() {
		serviceTracker.close();
	}

	@Override
	public void selectBackgroundProvider(BackgroundProvider background) {
		checkFxThread();
		if (background == null) {
			selectedId.set(null);
		} else {
			ServiceReference<BackgroundProvider> reference = serviceTracker.getServiceReference(background);
			if (reference == null) {
				throw new IllegalArgumentException(background + " is not registered as a service");
			}
			selectedId.set(ServiceUtils.getIdProperty(BackgroundProvider.PROPERTY_BACKGROUND_ID, reference, background));
		}
	}

	@Override
	public ObservableObjectValue<BackgroundProvider> getCurrentBackgroundProvider() {
		return currentBackgroundProvider;
	}

	@Override
	public ObservableList<BackgroundProvider> getBackgroundProviders() {
		return serviceTracker.getServiceList();
	}

	@Override
	public ObservableObjectValue<Background> getCurrentBackground() {
		return currentBackground;
	}

	@Override
	public BackgroundConfig store() {
		BackgroundConfig config = new BackgroundConfig();
		config.selectedId = selectedId.get();
		return config;
	}

	@Override
	public void restore(Optional<BackgroundConfig> memento) {
		memento.ifPresent(memo -> {
			if (memo.selectedId != null) {
				selectedId.set(memo.selectedId);
			}
		});
	}

	@Override
	public Class<? extends BackgroundConfig> getMementoType() {
		return BackgroundConfig.class;
	}

	@Override
	public void setObservableContext(ObservableContext ctx) {
		ctx.bind(selectedId);
	}

}
