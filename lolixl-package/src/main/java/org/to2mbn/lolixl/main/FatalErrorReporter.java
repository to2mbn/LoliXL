package org.to2mbn.lolixl.main;

import java.awt.Desktop;
import java.awt.Font;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

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
		showErrorDialog(generateErrorMessage());
	}

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

	private void showErrorDialog(String text) {
		// for copying style
		JLabel label = new JLabel();
		Font font = label.getFont();

		// create some css from the label's font
		StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
		style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
		style.append("font-size:" + font.getSize() + "pt;");

		// html content
		JEditorPane ep = new JEditorPane("text/html", "<html><body style=\"" + style + "\">"
				+ text
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

	private static String throwableToString(Throwable e) {
		CharArrayWriter writer = new CharArrayWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		e.printStackTrace(printWriter);
		printWriter.flush();
		return writer.toString();
	}
}
