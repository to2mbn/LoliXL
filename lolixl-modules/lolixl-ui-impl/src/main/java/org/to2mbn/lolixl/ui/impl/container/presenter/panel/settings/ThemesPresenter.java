package org.to2mbn.lolixl.ui.impl.container.presenter.panel.settings;

import com.sun.javafx.binding.StringConstant;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.event.WeakEventHandler;
import javafx.scene.input.MouseEvent;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.component.view.theme.ThemeTileView;
import org.to2mbn.lolixl.ui.impl.container.view.panel.settings.ThemesView;
import org.to2mbn.lolixl.ui.theme.Theme;
import org.to2mbn.lolixl.ui.theme.ThemeService;
import org.to2mbn.lolixl.utils.FXUtils;
import org.to2mbn.lolixl.utils.MappedObservableList;
import java.util.stream.Stream;

@Component(immediate = true)
public class ThemesPresenter extends Presenter<ThemesView> {

	private static final String FXML_LOCATION = "/ui/fxml/panel/themes_panel.fxml";

	@Reference
	private ThemeService themeService;

	private MappedObservableList<Theme, Tile> tilesMapping;

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
		tilesMapping = new MappedObservableList<>(themeService.getAllThemes(),
				theme -> {
					Tile tile = new Tile();
					FXUtils.setCssClass(tile, "xl-theme-tile"); // TODO
					ThemeTileView graphic = new ThemeTileView();
					graphic.nameLabel.textProperty().bind(theme.getLocalizedName());
					graphic.iconView.imageProperty().bind(theme.getIcon());
					FXUtils.setButtonGraphic(tile, graphic);
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
					return tile;
				});
		Bindings.bindContent(view.themesContainer.getChildren(), tilesMapping);
	}

	private void enableTheme(Tile tile, Theme theme) {
		if (!isThemeEnabled(theme)) {
			FXUtils.setCssClass(tile, "xl-theme-tile-enabled");
			themeService.enable(theme, false);
		}
	}

	private void disableTheme(Tile tile, Theme theme) {
		if (isThemeEnabled(theme)) {
			FXUtils.setCssClass(tile, "xl-theme-tile");
			themeService.disable(theme, false);
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
