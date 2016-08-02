package org.to2mbn.lolixl.ui;

import org.osgi.service.event.EventHandler;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;

public interface PresenterManagementService extends EventHandler {
	<T extends Presenter> T getPresenter(Class<T> type);
}
