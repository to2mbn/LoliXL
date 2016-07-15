package org.to2mbn.lolixl.ui.container.view;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.util.Objects;

public class DefaultFrameView {
	@FXML
	public BorderPane containerPane;

	@FXML
	public BorderPane titleBarPane;

	@FXML
	public BorderPane widgetPane;

	@FXML
	public BorderPane contentPane;

	public void setTitleBar(Parent titleBar) {
		Objects.requireNonNull(titleBar);
		if (titleBarPane != null) {
			titleBarPane.setCenter(titleBar);
		}
	}

	public void setWidget(Parent widget) {
		Objects.requireNonNull(widget);
		if (widgetPane != null) {
			widgetPane.setCenter(widget);
		}
	}

	public void setContent(Parent content) {
		Objects.requireNonNull(content);
		if (contentPane != null) {
			contentPane.setCenter(content);
		}
	}
}
