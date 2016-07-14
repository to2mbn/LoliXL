package org.to2mbn.lolixl.ui.container.presenter;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

@Component
@Service({DefaultUserProfilePresenter.class})
@Properties({
		@Property(name = "presenter.name", value = "default_user_profile_presenter")
})
public class DefaultUserProfilePresenter extends Presenter {
}
