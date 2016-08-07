package org.to2mbn.lolixl.ui.impl.container.presenter.panel.sidebar;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.core.game.version.GameVersion;
import org.to2mbn.lolixl.core.game.version.GameVersionProvider;
import org.to2mbn.lolixl.core.game.version.GameVersionProviderManager;
import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.component.view.version.GameVersionGroupView;
import org.to2mbn.lolixl.ui.component.view.version.GameVersionItemView;
import org.to2mbn.lolixl.ui.impl.container.view.panel.sidebar.GameVersionsView;
import org.to2mbn.lolixl.utils.CollectionUtils;
import org.to2mbn.lolixl.utils.MappedObservableList;

@Component(immediate = true)
public class GameVersionsPresenter extends Presenter<GameVersionsView> {

	private static final String FXML_LOCATION = "/ui/fxml/panel/game_versions_panel.fxml";

	@Reference
	private GameVersionProviderManager providerManager;

	@Activate
	public void active(ComponentContext compCtx) {
		super.active();
	}

	@Override
	protected void initializePresenter() {
		MappedObservableList<GameVersionProvider, GameVersionGroupView> mapped =
				new MappedObservableList<>(providerManager.getProviders(), this::makeViewForProvider);
		CollectionUtils.bindList(mapped, view.versionsContainer.getChildren());
	}

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
	}

	private GameVersionGroupView makeViewForProvider(GameVersionProvider provider) {
		GameVersionGroupView view = new GameVersionGroupView();
		Label pathLabel = view.mcdirPathLabel;
		StringBinding pathAliasBinding = new StringBinding() {
			private StringProperty aliasProperty = provider.aliasProperty();

			{
				bind(aliasProperty);
			}

			@Override
			protected String computeValue() {
				if (aliasProperty.get() != null) {
					Tooltip.install(view.mcdirPathLabel, new Tooltip(provider.getMinecraftDirectory().toString()));
					return aliasProperty.get();
				} else {
					Tooltip tooltip = view.mcdirPathLabel.getTooltip();
					if (tooltip != null) {
						Tooltip.uninstall(view.mcdirPathLabel, tooltip);
					}
					return provider.getMinecraftDirectory().toString();
				}
			}
		};
		pathLabel.textProperty().bind(pathAliasBinding);

		// make editable:
		TextField input = new TextField();
		input.setId("mcpath-editor");
		input.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				view.getChildren().remove(input);
				pathLabel.textProperty().bind(pathAliasBinding);
				view.setTop(pathLabel);
			}
		});
		pathLabel.setOnMouseMoved(event -> pathLabel.setUnderline(true));
		pathLabel.setOnMouseExited(event -> pathLabel.setUnderline(false));
		pathLabel.setOnMouseClicked(event -> {
			// init input
			if (pathLabel.getTooltip() == null) { // does not has alias
				input.setText(I18N.localize("org.to2mbn.lolixl.ui.impl.container.presenter.panel.sidebar.mcdirpath.editor.default.text").get());
			} else {
				input.setText(pathLabel.getText());
				pathLabel.textProperty().bind(input.textProperty());
				view.getChildren().remove(pathLabel);
				view.setTop(input);
			}
		});

		MappedObservableList<GameVersion, GameVersionItemView> mapped =
				new MappedObservableList<>(provider.getVersions(), this::makeViewForVersion);
		CollectionUtils.bindList(mapped, view.versionsContainer.getChildren());
		return view;
	}

	private GameVersionItemView makeViewForVersion(GameVersion version) {
		GameVersionItemView view = new GameVersionItemView();
		view.versionNameLabel.textProperty().bind(version.getLocalizedName());
		resolveTagsForVersion(view.versionTagContainer, version);
		return view;
	}

	private void resolveTagsForVersion(HBox tagContainer, GameVersion version) {
		// TODO
	}
}
