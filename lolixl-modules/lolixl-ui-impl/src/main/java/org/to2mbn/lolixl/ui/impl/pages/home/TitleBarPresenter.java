package org.to2mbn.lolixl.ui.impl.pages.home;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventAdmin;
import org.to2mbn.lolixl.ui.Presenter;
import org.to2mbn.lolixl.ui.impl.stage.MainStage;
import org.to2mbn.lolixl.utils.event.ApplicationExitEvent;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.stage.Stage;

@Service({ TitleBarPresenter.class })
@Component(immediate = true)
public class TitleBarPresenter extends Presenter<TitleBarView> {

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
		view.titleLabel.textProperty().bind(stage.titleProperty());
		view.closeButton.setOnMouseClicked(e -> Platform.runLater(() -> {
			stage.close();
			eventAdmin.postEvent(new ApplicationExitEvent());
		}));
		view.minimizeButton.setOnMouseClicked(e -> Platform.runLater(() -> stage.setIconified(true)));
		stage.focusedProperty().addListener(
				(dummy, oldVal, newVal) -> view.rootContainer.pseudoClassStateChanged(PseudoClass.getPseudoClass("window-focused"), newVal));
	}

	@Override
	protected String getFxmlLocation() {
		return "fxml/org.to2mbn.lolixl.ui.home/title_bar.fxml";
	}

}
