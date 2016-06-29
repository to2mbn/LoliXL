package org.to2mbn.lolixl;

import java.awt.Desktop;
import java.awt.Font;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

class Bootstrap {

	// TODO: 本地化错误提示
	private static final String MSG_JFX_TOO_OLD = "The JRE on this computer is too old. LoliXL requires Java 8u40 and greater.<br/>"
			+ "Click <a href=\"https://java.com/\">here</a> to download latest Java.<br/><br/>"
			+ "此计算机上的JRE版本过旧，LoliXL需要 Java 8u40 或更高的版本。<br/>"
			+ "点击<a href=\"https://java.com/\">这里</a>下载最新的Java。";

	public static void main(String[] args) {
		if (!isAbove8u40()) {
			showJRETooOldDialog();
			System.exit(1);
		}

		// TODO: 后续加载
	}

	private static boolean isAbove8u40() {
		try {
			Class.forName("javafx.scene.control.Dialog");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	private static void showJRETooOldDialog() {
		// for copying style
		JLabel label = new JLabel();
		Font font = label.getFont();

		// create some css from the label's font
		StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
		style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
		style.append("font-size:" + font.getSize() + "pt;");

		// html content
		JEditorPane ep = new JEditorPane("text/html", "<html><body style=\"" + style + "\">"
				+ MSG_JFX_TOO_OLD
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

		JOptionPane.showMessageDialog(null, ep, "LoliXL", JOptionPane.ERROR_MESSAGE);
	}

}
