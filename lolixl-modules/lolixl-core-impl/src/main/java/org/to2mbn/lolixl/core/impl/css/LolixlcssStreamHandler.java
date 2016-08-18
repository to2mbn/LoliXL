package org.to2mbn.lolixl.core.impl.css;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLStreamHandlerService;
import javafx.scene.text.Font;

@Service({ URLStreamHandlerService.class })
@Properties({
		@Property(name = "url.handler.protocol", value = "lolixlcss")
})
@Component(immediate = true)
public class LolixlcssStreamHandler extends AbstractURLStreamHandlerService {

	private static final Logger LOGGER = Logger.getLogger(LolixlcssStreamHandler.class.getCanonicalName());

	static final char[] CSS_PROPERTY_FONT_FAMILY = "-fx-font-family".toCharArray();

	private BundleContext bundleContext;

	@Activate
	public void active(ComponentContext compCtx) {
		bundleContext = compCtx.getBundleContext();
	}

	private String process(String in) {
		StringBuilder sb = new StringBuilder();
		char[] str = in.toCharArray();
		Set<String> families = new HashSet<>(Font.getFamilies());
		int matchCount = 0;
		for (int i = 0; i < str.length; i++) {
			char ch = str[i];
			if (ch == CSS_PROPERTY_FONT_FAMILY[matchCount]) {
				matchCount++;
			} else {
				matchCount = 0;
			}
			sb.append(ch);
			if (matchCount == CSS_PROPERTY_FONT_FAMILY.length) {
				matchCount = 0;
				int maohaoIdx = -1;
				for (int l = i + 1; l < str.length; l++) {
					char ch2 = str[l];
					if (ch2 != ' ' && ch2 != '\t') {
						if (ch2 == ':') {
							maohaoIdx = l;
						}
						break;
					}
				}
				if (maohaoIdx != -1) {
					List<String> fonts = new ArrayList<>();
					boolean escape = false;
					int inStr = 0;
					int strStartIdx = -1;
					for (int l = maohaoIdx + 1; l < str.length; l++) {
						char ch2 = str[l];
						if (ch2 != ' ' && ch2 != '\t' && ch2 != '\n' && ch2 != '\r' && ch2 != ',') {
							if (strStartIdx == -1) {
								strStartIdx = l;
							}
						} else if (inStr == 0 && strStartIdx != -1) {
							fonts.add(in.substring(strStartIdx, l));
							strStartIdx = -1;
						}
						if (escape) {
							escape = false;
						} else {
							if (ch2 == '\\') {
								escape = true;
							} else if (ch2 == '\'') {
								if (inStr == 0) {
									inStr = 1;
								} else if (inStr == 1) {
									inStr = 0;
								}
							} else if (ch2 == '"') {
								if (inStr == 0) {
									inStr = 2;
								} else if (inStr == 2) {
									inStr = 0;
								}
							} else if (inStr == 0) {
								if (ch2 == ';' || ch2 == '}') {
									if (strStartIdx != -1 && strStartIdx != l) {
										fonts.add(in.substring(strStartIdx, l));
									}
									String selected = null;
									for (String fontstr : fonts) {
										String family = convertUnicode(stripQuotes(fontstr));
										if (families.contains(family)) {
											selected = fontstr;
											break;
										}
									}
									if (selected == null) {
										LOGGER.fine("No font is available in " + fonts + ", skipping");
										sb.delete(sb.length() - CSS_PROPERTY_FONT_FAMILY.length, sb.length());
									} else {
										LOGGER.fine("Selected font " + selected + " from " + fonts);
										sb.append(": ").append(selected).append(';');
									}
									if (ch2 == '}') {
										sb.append('}');
									}
									i = l;
									break;
								}
							}
						}
					}
				}
			}
		}

		return sb.toString();
	}

	private String stripQuotes(String str) {
		int beginIndex = 0;
		char openQuote = str.charAt(beginIndex);
		if (openQuote == '\"' || openQuote == '\'') beginIndex += 1;
		int endIndex = str.length();
		char closeQuote = str.charAt(endIndex - 1);
		if (closeQuote == '\"' || closeQuote == '\'') endIndex -= 1;
		if ((endIndex - beginIndex) < 0) return str;
		return str.substring(beginIndex, endIndex);
	}

	private String convertUnicode(String src) {
		char[] buf;
		int bp;
		int buflen;
		char ch;
		int unicodeConversionBp = -1;
		buf = src.toCharArray();
		buflen = buf.length;
		bp = -1;
		char[] dst = new char[buflen];
		int dstIndex = 0;
		while (bp < buflen - 1) {
			ch = buf[++bp];
			if (ch == '\\') {
				if (unicodeConversionBp != bp) {
					bp++;
					ch = buf[bp];
					if (ch == 'u') {
						do {
							bp++;
							ch = buf[bp];
						} while (ch == 'u');
						int limit = bp + 3;
						if (limit < buflen) {
							char c = ch;
							int result = Character.digit(c, 16);
							if (result >= 0 && c > 0x7f) {
								ch = "0123456789abcdef".charAt(result);
							}
							int d = result;
							int code = d;
							while (bp < limit && d >= 0) {
								bp++;
								ch = buf[bp];
								char c1 = ch;
								int result1 = Character.digit(c1, 16);
								if (result1 >= 0 && c1 > 0x7f) {
									ch = "0123456789abcdef".charAt(result1);
								}
								d = result1;
								code = (code << 4) + d;
							}
							if (d >= 0) {
								ch = (char) code;
								unicodeConversionBp = bp;
							}
						}
					} else {
						bp--;
						ch = '\\';
					}
				}
			}
			dst[dstIndex++] = ch;
		}
		return new String(dst, 0, dstIndex);
	}

	private URL lookupResource(String host, String path) {
		long bundleId = Long.parseLong(host);
		Bundle bundle = bundleContext.getBundle(bundleId);
		if (bundle == null) {
			throw new IllegalArgumentException("Bundle " + bundleId + " not found");
		}
		return bundle.getResource(path);
	}

	@Override
	public URLConnection openConnection(URL u) throws IOException {
		URL resUrl = lookupResource(u.getHost(), u.getPath().substring(1));
		if (resUrl == null) {
			throw new IOException("Resource [" + u + "] not found");
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (InputStream in = resUrl.openStream()) {
			byte[] buf = new byte[8192];
			int read;
			while ((read = in.read(buf)) != -1) {
				out.write(buf, 0, read);
			}
		}
		InputStream instream;
		if ("true".equals(System.getProperty("lolixl.hackCss"))) {
			instream = new ByteArrayInputStream(process(new String(out.toByteArray(), "UTF-8")).getBytes("UTF-8"));
		} else {
			instream = new ByteArrayInputStream(out.toByteArray());
		}

		return new URLConnection(u) {

			@Override
			public void connect() throws IOException {}

			@Override
			public InputStream getInputStream() throws IOException {
				return instream;
			}
		};
	}

}
