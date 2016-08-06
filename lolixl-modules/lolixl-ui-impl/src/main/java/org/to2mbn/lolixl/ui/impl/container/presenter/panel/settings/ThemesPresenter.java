package org.to2mbn.lolixl.ui.impl.container.presenter.panel.settings;

import com.sun.javafx.binding.StringConstant;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.WeakListChangeListener;
import javafx.event.WeakEventHandler;
import javafx.scene.input.MouseEvent;
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
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.component.view.ThemeTileView;
import org.to2mbn.lolixl.ui.impl.container.view.panel.settings.ThemesView;
import org.to2mbn.lolixl.ui.impl.theme.ThemeServiceImpl;
import org.to2mbn.lolixl.ui.theme.Theme;
import org.to2mbn.lolixl.ui.theme.ThemeService;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

@Service({ org.osgi.service.event.EventHandler.class })
@Properties({
		@Property(name = EventConstants.EVENT_TOPIC, value = ConfigurationEvent.TOPIC_CONFIGURATION),
		@Property(name = EventConstants.EVENT_FILTER, value = "(" + ConfigurationEvent.KEY_CATEGORY + "=" + ThemeServiceImpl.CATEGORY_THEME_CONFIG + ")")
})
@Component(immediate = true)
public class ThemesPresenter extends Presenter<ThemesView> implements EventHandler {

	private static final String FXML_LOCATION = "/ui/fxml/panel/themes_panel.fxml";

	@Reference
	private ThemeService themeService;

	@Activate
	public void active(ComponentContext compCtx) {
		super.active();
	}

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
	}

	@Override
	protected void initializePresenter() {
		themeService.getAllThemes().addListener(new WeakListChangeListener<>(change -> refreshThemes()));
	}

	@Override
	public void handleEvent(Event event) {
		refreshThemes();
	}

	private void refreshThemes() {
		List<Tile> resolved = new LinkedList<>();
		themeService.getAllThemes().forEach(theme -> {
			Tile tile = new Tile();
			tile.setId("theme-tile");
			ThemeTileView graphic = new ThemeTileView(theme);
			tile.setGraphic(graphic);
			tile.setUserData(theme);
			tile.addEventHandler(MouseEvent.MOUSE_MOVED, new WeakEventHandler<>(event -> {
				updateInfoPane((Theme) tile.getUserData());
			}));
			tile.addEventHandler(MouseEvent.MOUSE_CLICKED, new WeakEventHandler<>(event -> {
				if (isThemeEnabled(theme)) {
					disableTheme(tile, theme);
				} else {
					enableTheme(tile, theme);
				}
			}));
			resolved.add(tile);
		});
		view.themesContainer.getChildren().setAll(resolved);
	}

	private void enableTheme(Tile tile, Theme theme) {
		if (!isThemeEnabled(theme)) {
			tile.setId(tile.getId() + "-installed");
			themeService.enable(theme);
		}
	}

	private void disableTheme(Tile tile, Theme theme) {
		if (isThemeEnabled(theme)) {
			tile.setId(tile.getId().replace("-installed", ""));
			themeService.disable(theme);
		}
	}

	private boolean isThemeEnabled(Theme theme) {
		return themeService.getEnabledThemes().contains(theme);
	}

	private void updateInfoPane(Theme selectedTheme) {
		view.themeNameLabel.textProperty().bind(selectedTheme.getLocalizedName());
		view.themeDescriptionLabel.textProperty().bind(selectedTheme.getDescription());
		view.themeAuthorsLabel.textProperty().bind(new StringBinding() {
			ObservableValue<ObservableStringValue[]> authorsBinding = selectedTheme.getAuthors();

			{
				bind(authorsBinding);
			}

			@Override
			protected String computeValue() {
				return Stream
					.of(authorsBinding.getValue())
					.reduce(StringConstant.valueOf(""), (str, next) -> Bindings.concat(str.get() + ", " + next.get()))
					.get();
			}
		});
	}
}
