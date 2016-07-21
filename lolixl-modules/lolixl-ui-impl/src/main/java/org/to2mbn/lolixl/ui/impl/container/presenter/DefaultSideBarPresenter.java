package org.to2mbn.lolixl.ui.impl.container.presenter;

import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.container.view.DefaultSidebarView;

import java.io.IOException;

public class DefaultSideBarPresenter extends Presenter<DefaultSidebarView> {
	private static final String LOCATION_OF_FXML = "/ui/fxml/container/default_side_bar.fxml";

	public void initialize() throws IOException {
		super.initialize(LOCATION_OF_FXML);
	}
}
