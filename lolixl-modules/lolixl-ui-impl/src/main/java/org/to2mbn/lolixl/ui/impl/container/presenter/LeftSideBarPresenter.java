package org.to2mbn.lolixl.ui.impl.container.presenter;

import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.util.Duration;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.core.game.auth.AuthenticationProfileManager;
import org.to2mbn.lolixl.ui.Panel;
import org.to2mbn.lolixl.ui.SideBarPanelDisplayService;
import org.to2mbn.lolixl.ui.container.presenter.Presenter;
import org.to2mbn.lolixl.ui.impl.component.model.PanelImpl;
import org.to2mbn.lolixl.ui.impl.container.view.LeftSidebarView;
import java.util.Objects;
import java.util.Optional;

@Service({ SideBarPanelDisplayService.class, LeftSideBarPresenter.class })
@Component(immediate = true)
public class LeftSideBarPresenter extends Presenter<LeftSidebarView> implements SideBarPanelDisplayService {

	private static final String FXML_LOCATION = "fxml/org.to2mbn.lolixl.ui.home/left_sidebar.fxml";

	private Panel currentPanel;

	@Reference
	private AuthenticationProfileManager authProfileManager;

	@Activate
	public void active(ComponentContext compCtx) {
		super.active();
	}

	@Override
	protected String getFxmlLocation() {
		return FXML_LOCATION;
	}

	@Override
	public Panel newPanel() {
		return new PanelImpl(this::showNewPanel, this::closeCurrentPanel);
	}

	@Override
	public Optional<Panel> getCurrent() {
		return currentPanel != null ? Optional.of(currentPanel) : Optional.empty();
	}

	@Override
	protected void initializePresenter() {
		view.sidebarContainer.getChildren().addListener((ListChangeListener<? super Node>) change -> {
			if (change.getList().size() > 0) {
				view.sidebarContainer.setId(view.sidebarContainer.getId().concat("-onpaneladded"));
			} else {
				view.sidebarContainer.setId(view.sidebarContainer.getId().replace("-onpaneladded", ""));
			}
		});
	}

	private void showNewPanel(Panel panel) {
		Objects.requireNonNull(panel);
		if (currentPanel != null) {
			currentPanel.hide();
		}
		currentPanel = panel;
		Parent pane = panel.contentProperty().get();
		pane.setVisible(false);
		view.sidebarContainer.getChildren().add(pane);
		Animation animation = generateAnimation(pane);
		pane.setVisible(true);
		animation.play();
	}

	private void closeCurrentPanel() {
		view.sidebarContainer.getChildren().clear();
		currentPanel = null;
	}

	private Animation generateAnimation(Parent pane) {
		TranslateTransition tran = new TranslateTransition(Duration.seconds(1), pane);
		double from = view.mainContentContainer.getLayoutX();
		tran.setFromX(from);
		tran.setToX(from + view.mainContentContainer.getWidth());
		return tran;
	}
}
