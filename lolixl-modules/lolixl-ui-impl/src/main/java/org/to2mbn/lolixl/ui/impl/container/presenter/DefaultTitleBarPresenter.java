package org.to2mbn.lolixl.ui.impl.container.presenter;

import javafx.beans.binding.Bindings;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventAdmin;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.MainStage;
import org.to2mbn.lolixl.ui.impl.container.view.DefaultTitleBarView;
import org.to2mbn.lolixl.utils.event.ApplicationExitEvent;

@Service({ DefaultTitleBarPresenter.class })
@Component(immediate = true)
public class DefaultTitleBarPresenter extends Presenter<DefaultTitleBarView> {
	private static final String FXML_LOCATION = "/ui/fxml/container/default_title_bar.fxml";

	@Reference
	private EventAdmin eventAdmin;

	@Reference(target = "(" + MainStage.PROPERTY_STAGE_ID + "=" + MainStage.MAIN_STAGE_ID + ")")
	private Stage stage;

	@Activate
	public void active(ComponentContext compCtx) {
		super.active();
	}

	@Override
	protected void initializePresenter() {
		view.minimizeButton.setOnMouseClicked(this::onMinimizeButtonClicked);
		view.closeButton.setOnMouseClicked(this::onCloseButtonClicked);
		view.rootContainer.idProperty().bind(Bindings
				.when(stage.focusedProperty())
				.then(view.rootContainer.idProperty().get().replace("-unfocused", ""))
				.otherwise(view.rootContainer.idProperty().get().concat("-unfocused")));
	}

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
	}

	private void onCloseButtonClicked(MouseEvent event) {
		eventAdmin.postEvent(new ApplicationExitEvent());
	}

	private void onMinimizeButtonClicked(MouseEvent event) {
		stage.hide();
	}
}
