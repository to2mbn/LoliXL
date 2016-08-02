package org.to2mbn.lolixl.ui.impl.container.presenter.panel.settings;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.to2mbn.lolixl.core.config.ConfigurationEvent;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.view.panel.settings.ThemesView;
import org.to2mbn.lolixl.ui.impl.theme.ThemeServiceImpl;
import org.to2mbn.lolixl.ui.theme.ThemeService;

@Service({ org.osgi.service.event.EventHandler.class, ThemesPresenter.class })
@Properties({
		@Property(name = EventConstants.EVENT_TOPIC, value = ConfigurationEvent.TOPIC_CONFIGURATION),
		@Property(name = EventConstants.EVENT_FILTER, value = "(" + ConfigurationEvent.KEY_CATEGORY + "=" + ThemeServiceImpl.CATEGORY_THEME_CONFIG + ")")
})
@Component(immediate = true)
public class ThemesPresenter extends Presenter<ThemesView> implements EventHandler<ActionEvent>, org.osgi.service.event.EventHandler {

	private static final String FXML_LOCATION = "/ui/fxml/panel/themes_panel.fxml";

	@Reference
	private ThemeService themeService;

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
