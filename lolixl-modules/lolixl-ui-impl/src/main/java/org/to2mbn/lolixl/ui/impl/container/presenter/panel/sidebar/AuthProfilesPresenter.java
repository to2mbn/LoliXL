package org.to2mbn.lolixl.ui.impl.container.presenter.panel.sidebar;

import javafx.event.WeakEventHandler;
import javafx.scene.input.MouseEvent;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.core.game.auth.AuthenticationProfile;
import org.to2mbn.lolixl.core.game.auth.AuthenticationProfileManager;
import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.ui.Panel;
import org.to2mbn.lolixl.ui.SideBarPanelDisplayService;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.presenter.DefaultSideBarPresenter;
import org.to2mbn.lolixl.ui.impl.container.view.panel.sidebar.AuthProfilesView;
import org.to2mbn.lolixl.utils.CollectionUtils;
import org.to2mbn.lolixl.utils.MappedObservableList;

@Component(immediate = true)
public class AuthProfilesPresenter extends Presenter<AuthProfilesView> {
	private static final String FXML_LOCATION = "/ui/fxml/panel/auth_types_panel.fxml";

	@Reference
	private AuthenticationProfileManager authProfileManager;

	@Reference
	private SideBarPanelDisplayService displayService;

	@Reference
	private DefaultSideBarPresenter sideBarPresenter;

	private MappedObservableList<AuthenticationProfile<?>, Tile> profileTiles;

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
		Tile tile = new Tile();
		tile.textProperty().bind(I18N.localize("org.to2mbn.lolixl.ui.impl.container.presenter.panel.sidebar.authtypes.configurebutton.text"));
		Panel panel = displayService.newPanel();
		panel.bindButton(tile);
		view.profilesContainer.getChildren().add(tile);

		profileTiles = new MappedObservableList<>(authProfileManager.getProfiles(), profile -> {
			Tile t = profile.createTile();
			t.addEventHandler(MouseEvent.MOUSE_CLICKED, new WeakEventHandler<>(event -> {
				// TODO: mark it as selected type
				sideBarPresenter.getView().userProfileContainer.getChildren().setAll(tile);
			}));
			return t;
		});
		CollectionUtils.bindList(profileTiles, view.profilesContainer.getChildren());
	}
}
