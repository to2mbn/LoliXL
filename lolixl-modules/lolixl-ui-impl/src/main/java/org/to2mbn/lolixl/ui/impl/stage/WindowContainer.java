package org.to2mbn.lolixl.ui.impl.stage;

import javafx.css.PseudoClass;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class WindowContainer extends Pane {

	public static final double SHADOW_WIDTH;

	static {
		if ("true".equals(System.getProperty("lolixl.windowShadow"))) {
			SHADOW_WIDTH = 15.0;
		} else {
			SHADOW_WIDTH = 0.0;
		}
	}

	private Region content;
	private Stage stage;
	private Pane shadowPane;

	private double dragX;
	private double dragY;
	private double initDx;
	private double initDy;

	public WindowContainer() {
		setStyle("-fx-background-color: transparent;");

		shadowPane = new Pane();
		shadowPane.setStyle("-fx-background-color: transparent;");
		shadowPane.setMouseTransparent(true);
		getChildren().add(shadowPane);
	}

	public void initContent(Region content) {
		this.content = content;
		content.getStyleClass().add("xl-shadow-stage");
		getChildren().add(content);
	}

	public void initStage(Stage stage) {
		this.stage = stage;
		stage.focusedProperty().addListener((dummy, oldVal, newVal) -> content.pseudoClassStateChanged(PseudoClass.getPseudoClass("window-focused"), newVal));
	}

	@Override
	protected void layoutChildren() {
		shadowPane.relocate(0, 0);
		shadowPane.resize(getWidth(), getHeight());
		content.relocate(SHADOW_WIDTH, SHADOW_WIDTH);
		content.resize(getWidth() - 2 * SHADOW_WIDTH, getHeight() - 2 * SHADOW_WIDTH);
	}

	public void setDraggable(Node node) {
		node.setOnMousePressed(event -> {
			if (event.isPrimaryButtonDown()) {
				dragX = event.getScreenX();
				dragY = event.getScreenY();
				initDx = dragX - stage.getX();
				initDy = dragY - stage.getY();
				event.consume();
			} else {
				dragX = Double.NaN;
				dragY = Double.NaN;
				initDx = Double.NaN;
				initDy = Double.NaN;
			}
		});
		node.setOnMouseDragged(event -> {
			if (!event.isPrimaryButtonDown() || Double.isNaN(dragX)) {
				return;
			}
			if (event.isStillSincePress()) {
				return;
			}
			dragX = event.getScreenX();
			dragY = event.getScreenY();
			node.setCursor(Cursor.HAND);
			stage.setX(dragX - initDx);
			stage.setY(dragY - initDy);

			event.consume();
		});
		node.setOnMouseReleased(event -> {
			if (stage.isResizable()) {
				node.setCursor(Cursor.DEFAULT);
				dragX = Double.NaN;
				dragY = Double.NaN;
				initDx = Double.NaN;
				initDy = Double.NaN;
			}
		});
	}

}
