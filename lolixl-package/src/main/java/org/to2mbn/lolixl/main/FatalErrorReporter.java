package org.to2mbn.lolixl.main;

import java.awt.Desktop;
import java.awt.Font;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

class FatalErrorReporter {

	private static final Logger LOGGER = Logger.getLogger(FatalErrorReporter.class.getCanonicalName());

	public static void process(Throwable e) {
		LOGGER.log(Level.SEVERE, "发生致命错误", e);
		new FatalErrorReporter(e).show();
	}

	private static final String BUG_REPORT_EMAIL = "yushijinhun@gmail.com";
	private static final String BUG_REPORT_URL = "https://github.com/to2mbn/LoliXL/issues";

	private Throwable e;

	public FatalErrorReporter(Throwable e) {
		this.e = Objects.requireNonNull(e);
	}

	public void show() {
		try {
			showFx();
		} catch (Throwable fxEx) {
			LOGGER.log(Level.WARNING, "Couldn't show JFX error dialog", fxEx);
			try {
				showSwing();
			} catch (Throwable swingEx) {
				LOGGER.log(Level.SEVERE, "Couldn't show Swing error dialog", swingEx);
			}
		}
	}

	// Swing
	private String generateErrorMessage() {
		String logPath = new File(Metadata.LOG_FILE).toURI().toString();

		StringBuilder sb = new StringBuilder();
		sb.append("LoliXL发生致命错误：\n");
		sb.append(throwableToString(e));
		sb.append('\n');
		sb.append("Maven版本：").append(Metadata.M2_GROUP_ID).append(':').append(Metadata.M2_ARTIFACT_ID).append(':').append(Metadata.M2_VERSION).append('\n');
		sb.append("日志文件：<a href=\"" + logPath + "\">" + logPath + "</a>");
		sb.append("\n\n");
		sb.append("我们为对您造成的不便深感抱歉，请将以上错误信息及日志发送到"
				+ "<a href=\"mailto:" + BUG_REPORT_EMAIL + "\">" + BUG_REPORT_EMAIL + "</a>"
				+ "，\n或反馈到"
				+ "<a href=\"" + BUG_REPORT_URL + "\">" + BUG_REPORT_URL + "</a>。");

		String result = sb.toString();
		result = result.replace("\n", "<br/>");
		result = result.replace(" ", "&nbsp;");
		result = result.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
		return result;
	}

	private void showSwing() {
		// for copying style
		JLabel label = new JLabel();
		Font font = label.getFont();

		// create some css from the label's font
		StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
		style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
		style.append("font-size:" + font.getSize() + "pt;");

		// html content
		JEditorPane ep = new JEditorPane("text/html", "<html><body style=\"" + style + "\">"
				+ generateErrorMessage()
				+ "</body></html>");

		// handle link events
		ep.addHyperlinkListener(new HyperlinkListener() {

			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
					if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (IOException | URISyntaxException ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		ep.setEditable(false);
		ep.setBackground(label.getBackground());

		JOptionPane.showMessageDialog(null, ep, Metadata.LOLIXL_NAME, JOptionPane.ERROR_MESSAGE);
	}
	//

	private static String throwableToString(Throwable e) {
		CharArrayWriter writer = new CharArrayWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		e.printStackTrace(printWriter);
		printWriter.flush();
		return writer.toString();
	}

	// === JFX
	private void showFx() throws InterruptedException, ExecutionException {
		new JFXPanel();
		CountDownLatch latch = new CountDownLatch(1);
		CompletableFuture.supplyAsync(() -> {
			Alert alert = new Alert(AlertType.ERROR);
			Scene scene = alert.getDialogPane().getScene();
			Stage stage = (Stage) scene.getWindow();
			stage.setAlwaysOnTop(true);
			alert.initOwner(null);
			alert.setTitle("LoliXL");
			alert.setHeaderText("LoliXL发生致命错误 ！");
			alert.setContentText(e.getClass().getName() + ": " + e.getMessage());
			Label label = new Label("The exception stacktrace was:");
			TextArea textArea = new TextArea(throwableToString(e));
			textArea.setEditable(false);
			textArea.setWrapText(true);
			textArea.setMaxWidth(Double.MAX_VALUE);
			textArea.setMaxHeight(Double.MAX_VALUE);
			GridPane.setVgrow(textArea, Priority.ALWAYS);
			GridPane.setHgrow(textArea, Priority.ALWAYS);
			GridPane expContent = new GridPane();
			expContent.setMaxWidth(Double.MAX_VALUE);
			expContent.add(label, 0, 0);
			expContent.add(textArea, 0, 1);
			VBox vbox = new VBox();
			Label mavenInfo = new Label(new StringBuilder()
					.append("Maven版本: ")
					.append(Metadata.M2_GROUP_ID)
					.append(':')
					.append(Metadata.M2_ARTIFACT_ID)
					.append(':')
					.append(Metadata.M2_VERSION)
					.toString());
			HBox logBox = new HBox();
			Label log = new Label("日志文件: ");
			String logUri = new File(Metadata.LOG_FILE).toURI().toString();
			Label logInfo = createHyperlinkLabel(logUri, logUri, scene);
			logBox.getChildren().addAll(log, logInfo);
			HBox feedbackBox1 = new HBox(), feedbackBox2 = new HBox();
			feedbackBox1.getChildren().addAll(
					new Label("我们为对您造成的不便深感抱歉，请将以上错误信息及日志发送到"),
					createHyperlinkLabel("mailto:" + BUG_REPORT_EMAIL, BUG_REPORT_EMAIL, scene));
			feedbackBox2.getChildren().addAll(
					new Label("或反馈到"),
					createHyperlinkLabel(BUG_REPORT_URL, BUG_REPORT_URL, scene));
			vbox.getChildren().addAll(new Label(), mavenInfo, logBox, new Label(), feedbackBox1, feedbackBox2);
			expContent.add(vbox, 0, 2);
			alert.getDialogPane().setExpandableContent(expContent);
			alert.getDialogPane().setExpanded(true);
			alert.getDialogPane().expandedProperty().addListener(dummy -> Platform.runLater(() -> {
				alert.getDialogPane().requestLayout();
				stage.sizeToScene();
			}));
			alert.show();
			return alert;
		}, Platform::runLater)
				.get()
				.setOnHidden(e -> latch.countDown());
		latch.await();
	}

	private static Label createHyperlinkLabel(String uri, String str, Scene scene) {
		Label result = new Label(str);
		result.setUnderline(true);
		result.setTextFill(Color.web("#0026ff"));
		result.setOnMouseClicked(e -> SwingUtilities.invokeLater(() -> { // call on EDT
			if (Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().browse(new URI(uri));
				} catch (IOException | URISyntaxException ex) {
					ex.printStackTrace();
				}
			}
		}));
		result.setOnMouseEntered(e -> scene.setCursor(Cursor.HAND));
		result.setOnMouseExited(e -> scene.setCursor(Cursor.DEFAULT));
		return result;
	}
	//
}
