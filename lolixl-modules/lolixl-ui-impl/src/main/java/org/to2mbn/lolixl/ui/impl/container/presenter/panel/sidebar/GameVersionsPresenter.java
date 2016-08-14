package org.to2mbn.lolixl.ui.impl.container.presenter.panel.sidebar;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.core.game.version.GameVersion;
import org.to2mbn.lolixl.core.game.version.GameVersionProvider;
import org.to2mbn.lolixl.core.game.version.GameVersionProviderManager;
import org.to2mbn.lolixl.core.game.version.tags.VersionTag;
import org.to2mbn.lolixl.core.game.version.tags.VersionTagResolver;
import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.ui.Panel;
import org.to2mbn.lolixl.ui.PanelDisplayService;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.component.view.version.GameVersionGroupView;
import org.to2mbn.lolixl.ui.component.view.version.GameVersionItemView;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.component.view.version.GameVersionEditorView;
import org.to2mbn.lolixl.ui.impl.component.view.version.GameVersionItemWrapperView;
import org.to2mbn.lolixl.ui.impl.container.presenter.DefaultSideBarPresenter;
import org.to2mbn.lolixl.ui.impl.container.view.panel.sidebar.GameVersionsView;
import org.to2mbn.lolixl.utils.FXUtils;
import org.to2mbn.lolixl.utils.MappedObservableList;

@Service({ GameVersionsPresenter.class })
@Component(immediate = true)
public class GameVersionsPresenter extends Presenter<GameVersionsView> {

	private static final String FXML_LOCATION = "/ui/fxml/panel/game_versions_panel.fxml";

	@Reference
	private GameVersionProviderManager providerManager;

	@Reference
	private DefaultSideBarPresenter sideBarPresenter;

	@Reference
	private PanelDisplayService displayService;

	@Reference
	private VersionTagResolver tagResolver;

	private MappedObservableList<GameVersionProvider, GameVersionGroupView> mappedGameVersionProviderViews;

	// FIXME: 变量意义不明
	// 1. 这是干嘛的
	// 2. 对于该变量，有可能的空指针访问
	// 3. 在该变量赋值处，仅仅将一个Provider的GameVersions赋值了过来
	//    如果说这是显示在侧边栏处的GameVersions，则此处设计有误。
	//    侧边栏应当显示所有Provider的GameVersions。
	private MappedObservableList<GameVersion, Tile> mappedGameVersionTiles;
	private Tile addNewVersionTile;

	@Activate
	public void active(ComponentContext compCtx) {
		super.active();
	}

	// TODO
	public void setAddNewVersionTile(Tile tile) {
		addNewVersionTile = tile;
		view.topContainer.getChildren().setAll(tile);
	}

	@Override
	protected void initializePresenter() {
		mappedGameVersionProviderViews = new MappedObservableList<>(providerManager.getProviders(), this::makeViewForProvider);
		Bindings.bindContent(view.versionsContainer.getChildren(), mappedGameVersionProviderViews);

		providerManager.selectedVersionProperty().addListener((ob, oldVal, newVal) -> {
			Node child = null;
			Tile cachedTile = mappedGameVersionTiles.mapping().get(newVal);
			if (newVal != null && cachedTile != null) {
				GameVersionItemWrapperView wrapper = new GameVersionItemWrapperView();
				wrapper.setCenter(cachedTile);
				child = wrapper;
			}
			if (child == null) {
				child = addNewVersionTile;
			}
			sideBarPresenter.getView().functionalTileTopContainer.getChildren().setAll(child);
		});
	}

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
	}

	private GameVersionGroupView makeViewForProvider(GameVersionProvider provider) {
		GameVersionGroupView groupView = new GameVersionGroupView();
		Label pathLabel = groupView.mcdirPathLabel;
		StringBinding pathAliasBinding = new StringBinding() {

			private StringProperty aliasProperty = provider.aliasProperty();

			{
				bind(aliasProperty);
			}

			@Override
			protected String computeValue() {
				if (aliasProperty.get() != null) {
					Tooltip.install(groupView.mcdirPathLabel, new Tooltip(provider.getMinecraftDirectory().toString()));
					return aliasProperty.get();
				} else {
					Tooltip tooltip = groupView.mcdirPathLabel.getTooltip();
					if (tooltip != null) {
						Tooltip.uninstall(groupView.mcdirPathLabel, tooltip);
					}
					return provider.getMinecraftDirectory().toString();
				}
			}
		};
		pathLabel.textProperty().bind(pathAliasBinding);

		// make editable:
		TextField input = new TextField();
		input.setId("-xl-mcpath-editor");
		input.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				groupView.setTop(null);
				pathLabel.textProperty().bind(pathAliasBinding);
				groupView.setTop(pathLabel);
			}
		});
		pathLabel.setOnMouseMoved(event -> pathLabel.setUnderline(true));
		pathLabel.setOnMouseExited(event -> pathLabel.setUnderline(false));
		pathLabel.setOnMouseClicked(event -> {
			// init input
			if (pathLabel.getTooltip() == null) { // does not have alias
				input.setText(I18N.localize("org.to2mbn.lolixl.ui.impl.container.presenter.panel.sidebar.mcdirpath.editor.default.text").get());
			} else {
				input.setText(pathLabel.getText());
			}
			provider.aliasProperty().bind(input.textProperty());
			groupView.setTop(null);
			Group group = new Group(input, makeDeleteMcdirButton(provider, groupView));
			groupView.setTop(group);
		});

		mappedGameVersionTiles = new MappedObservableList<>(provider.getVersions(), this::makeTileForVersion);
		Bindings.bindContent(view.versionsContainer.getChildren(), mappedGameVersionTiles);
		return groupView;
	}

	private Tile makeTileForVersion(GameVersion version) {
		Tile tile = version.createTile();

		GameVersionItemView item = (GameVersionItemView) tile.getGraphic();
		resolveTagsForVersion(item.versionTagContainer, version);

		GameVersionEditorView editorView = makeEditorForVersion(version);
		Panel editor = displayService.newPanel();
		editor.bindButton(tile);
		editor.bindItem(version);
		editor.contentProperty().set(editorView);
		editor.onClosedProperty().set(editorView.aliasInput.textProperty()::unbind);
		tile.setOnAction(event -> {
			providerManager.selectedVersionProperty().set(version);
			editor.show();
		});

		return tile;
	}

	private void resolveTagsForVersion(HBox tagContainer, GameVersion version) {
		ObservableList<Node> children = tagContainer.getChildren();
		tagResolver.resolveTags(version.getLaunchableVersion())
				.stream()
				.map(this::makeTagComponent)
				.forEach(children::add);
	}

	private Button makeTagComponent(VersionTag versionTag) {
		Button button = new Button();
		button.setPrefWidth(Region.USE_COMPUTED_SIZE);
		button.setPrefHeight(Region.USE_COMPUTED_SIZE);
		button.textProperty().bind(versionTag.getDisplayName());
		FXUtils.setCssClass(button, FXUtils.tagIdToCssClass(versionTag.getId()));
		return button;
	}

	private Button makeDeleteMcdirButton(GameVersionProvider provider, Parent parent) {
		Button button = new Button();
		button.setBackground(new Background(new BackgroundImage(new Image("/ui/img/delete_mcdir.png"), null, null, null, null)));
		button.setOnAction(event -> {
			// YUSHI'S TODO: delete the mcdir?
			view.versionsContainer.getChildren().remove(parent);
		});
		return button;
	}

	private GameVersionEditorView makeEditorForVersion(GameVersion version) {
		GameVersionEditorView editor = new GameVersionEditorView();
		editor.aliasInput.setText(version.aliasProperty().get());
		version.aliasProperty().bind(editor.aliasInput.textProperty());
		editor.mcdirPathContentLabel.setText(version.getMinecraftDirectory().toString());
		editor.versionNumberContentLabel.setText(version.getVersionNumber());
		GameVersionItemView itemView = (GameVersionItemView) mappedGameVersionTiles.mapping().get(version).getGraphic();
		editor.tagsContainer.getChildren().setAll(itemView.versionTagContainer.getChildren());
		return editor;
	}
}
