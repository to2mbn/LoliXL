package org.to2mbn.lolixl.ui.container.presenter;

import javafx.scene.layout.Background;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.lolixl.ui.container.view.DefaultFrameView;
import org.to2mbn.lolixl.ui.service.BackgroundManagingService;

@Component
@Service({DefaultFramePresenter.class, BackgroundManagingService.class})
public class DefaultFramePresenter extends Presenter<DefaultFrameView> implements BackgroundManagingService {
	@Override
	public void changeBackground(Background background) {
		view.rootPane.setBackground(background);
	}

	@Override
	public Background getCurrentBackground() {
		return view.rootPane.getBackground();
	}
}
