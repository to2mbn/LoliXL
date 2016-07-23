package org.to2mbn.lolixl.ui.container.panelcontent;

import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.container.view.View;

public abstract class PanelContentPresenter<T extends View> extends Presenter<T> {
	public void onPanelShown() {
	}

	public void onPanelClosed() {
	}
}
