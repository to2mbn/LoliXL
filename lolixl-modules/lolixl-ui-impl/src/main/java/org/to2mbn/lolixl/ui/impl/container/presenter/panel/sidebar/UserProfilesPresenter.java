package org.to2mbn.lolixl.ui.impl.container.presenter.panel.sidebar;

import javafx.scene.input.MouseEvent;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.core.game.auth.AuthenticationProfileManager;
import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.ui.Panel;
import org.to2mbn.lolixl.ui.SideBarPanelDisplayService;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.view.panel.sidebar.UserProfilesView;

@Component(immediate = true)
public class UserProfilesPresenter extends Presenter<UserProfilesView> {
	private static final String FXML_LOCATION = "/ui/fxml/panel/auth_types_panel.fxml";

	@Reference
	private AuthenticationProfileManager authProfileManager;

	@Reference
	private SideBarPanelDisplayService displayService;

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
		// TODO FOR YUSHI: ↓ observable
		authProfileManager.getProfiles().stream()
				.map(profile -> profile.createTile())
				.forEach(tile -> {
					tile.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
						// TODO: mark it as selected type
					});
					view.typesContainer.getChildren().add(tile);
				});

		Tile tile = new Tile();
		tile.textProperty().bind(I18N.localize("org.to2mbn.lolixl.ui.impl.container.presenter.panel.sidebar.authtypes.configurebutton.text"));
		Panel panel = displayService.newPanel();
		panel.bindButton(tile);
		view.typesContainer.getChildren().add(tile);
	}
}
