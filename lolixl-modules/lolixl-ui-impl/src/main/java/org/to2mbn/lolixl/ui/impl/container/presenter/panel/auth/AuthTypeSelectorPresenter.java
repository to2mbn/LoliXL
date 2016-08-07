package org.to2mbn.lolixl.ui.impl.container.presenter.panel.auth;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.core.game.auth.AuthenticationProfileManager;
import org.to2mbn.lolixl.core.game.auth.AuthenticationService;
import org.to2mbn.lolixl.ui.component.Tile;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.view.panel.auth.AuthTypeSelectorView;
import org.to2mbn.lolixl.utils.CollectionUtils;
import org.to2mbn.lolixl.utils.MappedObservableList;
import org.to2mbn.lolixl.utils.ObservableServiceTracker;

@Component
public class AuthTypeSelectorPresenter extends Presenter<AuthTypeSelectorView> {
	private static final String FXML_LOCATION = "/ui/fxml/panel/auth_type_selector_panel.fxml";

	@Reference
	private AuthenticationProfileManager profileManager;

	private ObservableServiceTracker<AuthenticationService> serviceTracker;
	private MappedObservableList<AuthenticationService, Tile> tilesMapping;

	@Activate
	public void active(ComponentContext compCtx) {
		super.active();
		BundleContext bundleCtx = compCtx.getBundleContext();
		serviceTracker = new ObservableServiceTracker<>(bundleCtx, AuthenticationService.class);
		tilesMapping = new MappedObservableList<>(serviceTracker.getServiceList(), AuthenticationService::createTile);
		serviceTracker.open(true);
	}

	@Override
	protected void initializePresenter() {
		CollectionUtils.bindList(tilesMapping, view.tilesContainer.getChildren());
	}

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
	}
}
