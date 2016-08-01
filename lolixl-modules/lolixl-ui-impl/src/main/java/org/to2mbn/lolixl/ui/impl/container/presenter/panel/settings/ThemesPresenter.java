package org.to2mbn.lolixl.ui.impl.container.presenter.panel.settings;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.service.event.Event;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.UIApp;
import org.to2mbn.lolixl.ui.impl.container.view.panel.settings.ThemesView;
import org.to2mbn.lolixl.ui.theme.Theme;
import org.to2mbn.lolixl.ui.theme.ThemeService;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class ThemesPresenter extends Presenter<ThemesView> implements EventHandler<ActionEvent>, org.osgi.service.event.EventHandler {

	private static final String FXML_LOCATION = "/ui/fxml/panel/themes_panel.fxml";

	@Reference
	private ThemeService themeService;

	private UIApp uiApp;
	private Map<Tile, Theme> tileThemeMap = new HashMap<>();
	private ImageView themeInstalledMark;

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
	}

	@Override
	public void postInitialize() {
		refreshThemeTiles();
		themeInstalledMark = new ImageView("/ui/img/theme_installed.png");
	}

	public void setUiApp(UIApp _uiApp) {
		uiApp = _uiApp;
	}

	@Override
	public void handle(ActionEvent event) {
		Tile clickedTile = (Tile) event.getSource();
		StackPane imgContainer = (StackPane) clickedTile.getGraphic();
		Theme theme = tileThemeMap.get(clickedTile);
		if (!uiApp.isThemeInstalled(theme)) {
			themeService.enable(theme);
			imgContainer.setAlignment(Pos.CENTER_LEFT);
			imgContainer.getChildren().add(themeInstalledMark);
		} else {
			themeService.disable(theme);
			imgContainer.getChildren().remove(themeInstalledMark);
		}
	}

	private Tile makeTile() {
		Tile tile = new Tile();
		tile.setAlignment(Pos.BOTTOM_CENTER);
		tile.resize(100, 100);
		return tile;
	}

	private void showFileSelector() {
		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(new File("."));
		chooser.setTitle(""); // TODO
		chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("", "zip")); // TODO
		File file = chooser.showOpenDialog(uiApp.getMainStage());
		if (file != null) {
			Optional<Theme> loaded = null;
			// TODO: Load an zipped theme package
			if (loaded.isPresent()) {
				refreshThemeTiles();
			}
		}
	}

	private void addTileForTheme(Theme theme) {
		Tile tile = makeTile();
		tile.setText();
		tile.setOnMouseMoved(event -> updateThemeInfoLabels(theme));
		tile.setOnAction(this);

		StackPane imgContainer = new StackPane();
		imgContainer.resize(100, 100);
		Image icon;
		if (theme.getIcon() != null) {
			icon = theme.getIcon();
		} else {
			icon = new Image(getClass().getResourceAsStream("/ui/img/theme_empty_icon.png"));
		}
		imgContainer.getChildren().add(new ImageView(icon));
		if (uiApp.isThemeInstalled(theme)) {
			imgContainer.setAlignment(Pos.CENTER_LEFT);
			imgContainer.getChildren().add(themeInstalledMark);
		}
		tile.setGraphic(imgContainer);

		view.themesContainer.getChildren().add(tile);
		tileThemeMap.put(tile, theme);
	}

	private void refreshThemeTiles() {
		view.themesContainer.getChildren().clear();
		uiApp.getAllThemes().forEach(this::addTileForTheme);
		Tile installThemeTile = makeTile();
		installThemeTile.setOnAction(event -> showFileSelector());
		// TODO: icon
		view.themesContainer.getChildren().add(installThemeTile);
	}

	private void updateThemeInfoLabels(Theme theme) {
		view.themeNameLabel.setText(theme.getId()); // TODO
		view.themeAuthorsLabel.setText(Stream.of(theme.getAuthors()).reduce("", (string, author) -> string + ", " + author));
		view.themeDescriptionLabel.setText(theme.getDescription());
	}

	@Override
	public void handleEvent(Event event) {
		// TODO: 这是药丸
	}
}
