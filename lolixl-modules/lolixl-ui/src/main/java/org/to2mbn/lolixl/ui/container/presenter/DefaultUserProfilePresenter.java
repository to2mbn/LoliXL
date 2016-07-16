package org.to2mbn.lolixl.ui.container.presenter;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.lolixl.ui.container.view.DefaultUserProfileView;

@Component
@Service({DefaultUserProfilePresenter.class})
public class DefaultUserProfilePresenter extends Presenter<DefaultUserProfileView> {
}
