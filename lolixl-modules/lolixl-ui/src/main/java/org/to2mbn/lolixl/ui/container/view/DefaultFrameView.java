package org.to2mbn.lolixl.ui.container.view;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

import java.util.Objects;

public class DefaultFrameView {
	@FXML
	private BorderPane containerPane;

	@FXML
	private BorderPane titleBarPane;

	@FXML
	private BorderPane widgetPane;

	@FXML
	private BorderPane contentPane;

	public void setTitleBar(Region titleBar) {
		Objects.requireNonNull(titleBar);
		if (titleBarPane != null) {
			titleBarPane.setCenter(titleBar);
		}
	}

	public void setWidget(Region widget) {
		Objects.requireNonNull(widget);
		if (widgetPane != null) {
			widgetPane.setCenter(widget);
		}
	}

	public void setContent(Region content) {
		Objects.requireNonNull(content);
		if (contentPane != null) {
			contentPane.setCenter(content);
		}
	}
}
