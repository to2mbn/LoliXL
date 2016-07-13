package org.to2mbn.lolixl.ui.container.presenter;

import javafx.scene.Parent;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.lolixl.ui.container.view.DefaultFrameView;

@Component
@Service({Presenter.class})
@Properties({
		@Property(name = "presenter.name", value = "default_frame_presenter")
})
public class DefaultFramePresenter extends Presenter<DefaultFrameView, Parent> {
}
