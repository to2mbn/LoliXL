package org.to2mbn.lolixl.ui;

import javafx.application.Application;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.osgi.service.component.ComponentContext;

@Component
public class UIBootstrap {
	@Activate
	public void active(ComponentContext compCtx) {
		Application.launch(UIApp.class);
	}
}
