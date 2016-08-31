package org.to2mbn.lolixl.ui.impl.panel;

import javafx.beans.InvalidationListener;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import org.to2mbn.lolixl.ui.panel.Panel;
import org.to2mbn.lolixl.utils.BundleUtils;
import org.to2mbn.lolixl.utils.CollectionUtils;
import org.to2mbn.lolixl.utils.FXUtils;
import java.io.IOException;
import java.io.UncheckedIOException;

public class PanelView extends BorderPane {

	private static final String FXML_LOCATION = "fxml/org.to2mbn.lolixl.ui.panel/panel.fxml";

	private double iconW = 45.0;
	private double iconH = 45.0;

	@FXML
	public HBox headerContainer;

	@FXML
	public StackPane previousButtonContainer;

	@FXML
	public Circle previousCircle;

	@FXML
	public SVGPath previousButton;

	@FXML
	public ImageView iconView;

	@FXML
	public Label titleLabel;

	@FXML
	public StackPane panelContentContainer;

	public final Panel model;

	public PanelView(Panel model) {
		this.model = model;
		FXUtils.checkFxThread();
		FXMLLoader loader = new FXMLLoader(BundleUtils.getResourceFromBundle(getClass(), FXML_LOCATION));
		loader.setRoot(this);
		loader.setController(this);
		try {
			loader.load();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		initComponent();
	}

	private void initComponent() {
		setAlignment(headerContainer, Pos.TOP_LEFT);
		initPreviousButton();
		titleLabel.setLabelFor(iconView);
		titleLabel.textProperty().bind(model.titleProperty());
		CollectionUtils.bindSingleton(model.contentProperty(), panelContentContainer.getChildren());
		iconView.imageProperty().bind(model.iconProperty());
		iconView.imageProperty().addListener((observable, oldValue, newValue) -> checkEmptyIcon());
		checkEmptyIcon();
	}

	private void initPreviousButton() {
		previousCircle.radiusProperty().set(previousButton.prefWidth(-1) / 2);
		double pW = previousButtonContainer.prefWidth(-1);
		double pH = previousButtonContainer.prefHeight(-1);
		previousButtonContainer.setScaleX(iconW / pW);
		previousButtonContainer.setScaleY(iconH / pH);
		double paddingH = (iconW - pW) / 2;
		double paddingV = (iconH - pH) / 2;
		HBox.setMargin(previousButtonContainer, new Insets(paddingV, paddingH, paddingV, paddingH));
		InvalidationListener onHoveredChange = dummy -> previousCircle.pseudoClassStateChanged(PseudoClass.getPseudoClass("button-hovered"),
				previousCircle.isHover() || previousButton.isHover());
		previousCircle.hoverProperty().addListener(onHoveredChange);
		previousButton.hoverProperty().addListener(onHoveredChange);
		InvalidationListener onPressedChange = dummy -> previousCircle.pseudoClassStateChanged(PseudoClass.getPseudoClass("button-pressed"),
				previousCircle.isPressed() || previousButton.isPressed());
		previousCircle.pressedProperty().addListener(onPressedChange);
		previousButton.pressedProperty().addListener(onPressedChange);
		EventHandler<MouseEvent> onClick = event -> model.hide();
		previousCircle.setOnMouseClicked(onClick);
		previousButton.setOnMouseClicked(onClick);
	}

	private void checkEmptyIcon() {
		if (iconView.getImage() == null) {
			headerContainer.getChildren().remove(iconView);
		} else if (!headerContainer.getChildren().contains(iconView)) {
			headerContainer.getChildren().add(iconView);
		}
	}
}
