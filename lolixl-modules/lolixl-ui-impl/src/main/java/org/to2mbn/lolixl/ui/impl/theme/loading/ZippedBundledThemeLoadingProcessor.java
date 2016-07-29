package org.to2mbn.lolixl.ui.impl.theme.loading;

import org.apache.felix.scr.annotations.*;
import org.apache.felix.scr.annotations.Properties;
import org.to2mbn.lolixl.ui.theme.Theme;
import org.to2mbn.lolixl.ui.theme.loading.ThemeLoadingProcessor;
import org.to2mbn.lolixl.utils.GsonUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
@Service({ ThemeLoadingProcessor.class })
@Properties({ @Property(name = "name", value = "zipped_processor") })
public class ZippedBundledThemeLoadingProcessor implements ThemeLoadingProcessor {
	private static final Logger LOGGER = Logger.getLogger(ZippedBundledThemeLoadingProcessor.class.getCanonicalName());

	@Reference(target = "(usage=local_io)")
	private ExecutorService localIOPool;

	@Override
	public Predicate<URL> getChecker() {
		return url -> url.getProtocol().equalsIgnoreCase("file") && url.getFile().endsWith(".zip");
	}

	@Override
	public Theme process(URL baseUrl) throws IOException {
		LOGGER.fine("Started processing bundled theme: " + baseUrl.toExternalForm());
		try {
			return localIOPool.submit(() -> {
				URI bundleURI = URI.create("jar:" + baseUrl.toExternalForm());
				Map<String, Object> metaMap = new HashMap<>();
				try (FileSystem bundleFileSystem = FileSystems.newFileSystem(bundleURI, Collections.emptyMap())) {
					Path meta = bundleFileSystem.getPath("/" + Theme.PROPERTY_FILE_NAME);
					metaMap.putAll(GsonUtils.fromJson(meta, metaMap.getClass()));
					metaMap.put(Theme.INTERNAL_PROPERTY_KEY_PACKAGE_URL, baseUrl.toExternalForm());
				}

				ZipInputStream stream = new ZipInputStream(baseUrl.openStream());
				ClassLoader resourceLoader = new URLClassLoader(new URL[]{ baseUrl });
				List<String> styleSheets = new ArrayList<>();
				ZipEntry entry;
				while ((entry = stream.getNextEntry()) != null) {
					if (!entry.isDirectory() && entry.getName().endsWith(".css")) {
						styleSheets.add("/" + entry.getName());
					}
				}
				stream.close();

				return new Theme() {
					@Override
					public ClassLoader getResourceLoader() {
						return resourceLoader;
					}

					@Override
					public String getId() {
						Object id = metaMap.get(Theme.PROPERTY_KEY_ID);
						return id != null ? (String) id : "";
					}

					@Override
					public Map<String, Object> getMeta() {
						return metaMap;
					}

					@Override
					public String[] getStyleSheets() {
						return styleSheets.toArray(new String[styleSheets.size()]);
					}
				};
			}).get();
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.log(Level.WARNING, "Failed to process theme: " + baseUrl.toExternalForm(), e);
			throw new IOException(e);
		}
	}
}
