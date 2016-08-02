package org.to2mbn.lolixl.ui.impl.container.presenter.panel.settings;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.to2mbn.lolixl.core.config.ConfigurationEvent;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.view.panel.settings.ThemesView;
import org.to2mbn.lolixl.ui.impl.theme.ThemeServiceImpl;
import org.to2mbn.lolixl.ui.theme.ThemeService;
import java.util.Dictionary;
import java.util.Hashtable;

@Component
public class ThemesPresenter extends Presenter<ThemesView> implements EventHandler<ActionEvent>, org.osgi.service.event.EventHandler {

	private static final String FXML_LOCATION = "/ui/fxml/panel/themes_panel.fxml";

	@Reference
	private ThemeService themeService;

	public ThemesPresenter(BundleContext ctx) {
		super(ctx);
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put(EventConstants.EVENT_TOPIC, ConfigurationEvent.TOPIC_CONFIGURATION);
		properties.put(EventConstants.EVENT_FILTER, "(" + ConfigurationEvent.KEY_CATEGORY + "=" + ThemeServiceImpl.CATEGORY_THEME_CONFIG + ")");
		ctx.registerService(org.osgi.service.event.EventHandler.class, this, properties);
	}

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
	}

	@Override
	public void postInitialize() {
	}

	@Override
	public void handle(ActionEvent event) {
	}

	@Override
	public void handleEvent(Event event) {
		// TODO: 刷新Theme列表
	}
}
