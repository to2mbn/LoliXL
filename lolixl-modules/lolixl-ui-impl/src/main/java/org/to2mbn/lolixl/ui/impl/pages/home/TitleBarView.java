package org.to2mbn.lolixl.ui.impl.pages.home;

import org.to2mbn.lolixl.ui.View;
import org.to2mbn.lolixl.utils.FXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

public class TitleBarView extends View {

	private double btnW = 30.0;
	private double btnH = 30.0;
	private double iconProportion = 0.35;

	private double w = btnW * iconProportion;
	private double h = btnH * iconProportion;

	@FXML
	public BorderPane rootContainer;

	@FXML
	public Label titleLabel;

	@FXML
	public HBox buttonContainer;

	@FXML
	public Button closeButton;

	@FXML
	public Button minimizeButton;

	@FXML
	private void initialize() {
		BorderPane.setAlignment(titleLabel, Pos.CENTER_LEFT);
		rootContainer.maxHeightProperty().set(Region.USE_PREF_SIZE);
		rootContainer.minHeightProperty().set(Region.USE_PREF_SIZE);

		initButton(closeButton, createXShape());

		initButton(minimizeButton, create_Shape());
		double bottomPadding = (btnH - h) / 2.0;
		minimizeButton.setPadding(new Insets(0, 0, bottomPadding, 0));
	}

	private void initButton(Button button, Shape icon) {
		icon.getStyleClass().add("xl-title-bar-button-shape");
		button.getStyleClass().setAll("xl-title-bar-button");
		button.setGraphic(icon);
		FXUtils.setSizeToPref(button);
	}

	private Shape createXShape() {
		Line l1 = new Line(0, 0, w, h);
		Line l2 = new Line(w, 0, 0, h);
		Shape shapeX = Shape.union(l1, l2);
		shapeX.setId("xl-title-bar-close-button-shape");
		return shapeX;
	}

	private Shape create_Shape() {
		Line line_ = new Line(0, 0, w, 0);
		line_.setId("xl-title-bar-minimize-button-shape");
		return line_;
	}
}
