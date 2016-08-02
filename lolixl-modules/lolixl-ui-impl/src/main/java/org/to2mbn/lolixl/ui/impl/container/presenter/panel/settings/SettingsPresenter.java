package org.to2mbn.lolixl.ui.impl.container.presenter.panel.settings;

import javafx.application.Platform;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.to2mbn.lolixl.core.config.ConfigurationCategory;
import org.to2mbn.lolixl.ui.SettingsCategoriesManagingService;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.view.panel.settings.SettingsView;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

@SuppressWarnings("rawtypes")

@Service({ SettingsCategoriesManagingService.class, SettingsPresenter.class })
@Component(immediate = true)
public class SettingsPresenter extends Presenter<SettingsView> implements SettingsCategoriesManagingService {

	private static final String FXML_LOCATION = "/ui/fxml/panel/settings_panel.fxml";

	private ServiceTracker<ConfigurationCategory, ConfigurationCategory> serviceTracker;
	private Deque<ConfigurationCategory<?>> categories;

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
	}

	@Override
	public void postInitialize() {
		categories = new ConcurrentLinkedDeque<>();
		BundleContext ctx = FrameworkUtil.getBundle(getClass()).getBundleContext();
		serviceTracker = new ServiceTracker<>(ctx, ConfigurationCategory.class, new ServiceTrackerCustomizer<ConfigurationCategory, ConfigurationCategory>() {

			@Override
			public ConfigurationCategory addingService(ServiceReference<ConfigurationCategory> reference) {
				ConfigurationCategory<?> category = ctx.getService(reference);
				categories.addLast(category);
				Platform.runLater(SettingsPresenter.this::refreshCategories);
				return category;
			}

			@Override
			public void modifiedService(ServiceReference<ConfigurationCategory> reference, ConfigurationCategory service) {}

			@Override
			public void removedService(ServiceReference<ConfigurationCategory> reference, ConfigurationCategory service) {
				categories.remove(service);
				ctx.ungetService(reference);
			}
		});
		serviceTracker.open();
		view.categoryContainer.selectionModelProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue != null) {
				view.contentContainer.getChildren().clear();
			}
			view.contentContainer.getChildren().add(newValue.getSelectedItem().createConfiguringPanel());
		});
	}

	public void onShown() {
		refreshCategories();
	}

	private void refreshCategories() {
		view.categoryContainer.getItems().setAll(categories);
	}
}
