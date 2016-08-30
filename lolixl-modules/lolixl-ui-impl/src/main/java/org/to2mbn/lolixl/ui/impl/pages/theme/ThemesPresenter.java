package org.to2mbn.lolixl.ui.impl.pages.theme;

import static java.util.stream.Collectors.*;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.ui.Presenter;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.theme.Theme;
import org.to2mbn.lolixl.ui.theme.ThemeService;
import org.to2mbn.lolixl.utils.CollectionUtils;
import org.to2mbn.lolixl.utils.FXUtils;
import org.to2mbn.lolixl.utils.MappedObservableList;
import java.util.function.Consumer;
import java.util.stream.Stream;

// TODO: 在重构完毕后令enabled=true
@Component(immediate = true, enabled = false)
public class ThemesPresenter extends Presenter<ThemesView> {

	private static final String FXML_LOCATION = "/ui/fxml/panel/themes_panel.fxml";

	@Reference
	private ThemeService themeService;

	private MappedObservableList<Theme, Tile> tilesMapping;

	// 用于维护WeakListener的引用
	@SuppressWarnings("unused")
	private InvalidationListener enabledThemesListener;
	@SuppressWarnings("unused")
	private InvalidationListener disabledThemesListener;

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
					tile.getStyleClass().add("xl-theme-tile");
					ThemeTileView graphic = new ThemeTileView();
					graphic.nameLabel.textProperty().bind(theme.getLocalizedName());
					graphic.iconView.imageProperty().bind(theme.getIcon());
					FXUtils.setButtonGraphic(tile, graphic);
					tile.setUserData(theme);
					tile.addEventHandler(MouseEvent.MOUSE_MOVED, event -> updateInfoPane((Theme) tile.getUserData()));
					tile.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
						if (isThemeEnabled(theme)) {
							disableTheme(tile, theme);
						} else {
							enableTheme(tile, theme);
						}
					});
					return tile;
				});
		enabledThemesListener = bindCssClass(themeService.getEnabledThemes(), "xl-theme-tile-enabled");
		disabledThemesListener = bindCssClass(themeService.getDisabledThemes(), "xl-theme-tile-disabled");
		Bindings.bindContent(view.themesContainer.getChildren(), tilesMapping);
	}

	private InvalidationListener bindCssClass(ObservableList<Theme> themes, String cssClass) {
		return CollectionUtils.addDiffListener(themes, operateCssClass(added -> added.add(cssClass)), operateCssClass(removed -> removed.remove(cssClass)));
	}

	private Consumer<Theme> operateCssClass(Consumer<ObservableList<String>> operation) {
		return theme -> Platform.runLater(() -> operation.accept(tilesMapping.mapping().get(theme).getStyleClass()));
	}

	private void enableTheme(Tile tile, Theme theme) {
		if (!isThemeEnabled(theme)) {
			themeService.enable(theme, false);
		}
	}

	private void disableTheme(Tile tile, Theme theme) {
		if (isThemeEnabled(theme)) {
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
						.map(ObservableStringValue::get)
						.collect(joining(", "));
			}
		});
	}
}
