package org.to2mbn.lolixl.ui.container.view;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.util.Objects;

public class DefaultFrameView extends View {
	@FXML
	public BorderPane rootPane;

	@FXML
	public BorderPane titleBarPane;

	@FXML
	public BorderPane sidebarPane;

	@FXML
	public BorderPane contentPane;

	public void setTitleBar(Parent titleBar) {
		Objects.requireNonNull(titleBar);
		if (titleBarPane != null) {
			titleBarPane.setCenter(titleBar);
		}
	}

	public void setSidebar(Parent sidebar) {
		Objects.requireNonNull(sidebar);
		if (sidebarPane != null) {
			sidebarPane.setCenter(sidebar);
		}
	}

	public void setContent(Parent content) {
		Objects.requireNonNull(content);
		if (contentPane != null) {
			contentPane.setCenter(content);
		}
	}
}
