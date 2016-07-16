package org.to2mbn.lolixl.ui.container.presenter.content;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.container.view.content.HomeContentView;
import org.to2mbn.lolixl.ui.service.DisplayService;

import java.io.IOException;
import java.net.URL;

@Component
@Service({HomeContentPresenter.class})
public class HomeContentPresenter extends Presenter<HomeContentView> {
	@Reference
	private DisplayService displayService;

	@Override
	public void initialize(URL fxmlLocation) throws IOException {
		super.initialize(fxmlLocation);
		view.homeTile.setOnAction(event -> displayService.displayPane(view.rootContainer));
	}
}
