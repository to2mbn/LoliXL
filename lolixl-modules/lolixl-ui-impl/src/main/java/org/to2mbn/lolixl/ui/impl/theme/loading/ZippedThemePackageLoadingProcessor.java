package org.to2mbn.lolixl.ui.impl.theme.loading;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.lolixl.ui.theme.Theme;
import org.to2mbn.lolixl.ui.theme.loading.ThemeLoadingProcessor;
import org.to2mbn.lolixl.utils.GsonUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Logger;

@Component
@Service({ ThemeLoadingProcessor.class })
@Properties({ @Property(name = "name", value = "zipped_processor") })
public class ZippedThemePackageLoadingProcessor implements ThemeLoadingProcessor {
	private static final Logger LOGGER = Logger.getLogger(ZippedThemePackageLoadingProcessor.class.getCanonicalName());

	@Override
	public Predicate<URL> getChecker() {
		return url -> url.getProtocol().equalsIgnoreCase("file") && url.getFile().endsWith(".zip");
	}

	@Override
	public Theme process(URL baseUrl) throws IOException {
		LOGGER.fine("Started processing bundled theme: " + baseUrl.toExternalForm());
		URI bundleURI = URI.create("jar:" + baseUrl.toExternalForm());
		ClassLoader resourceLoader = new URLClassLoader(new URL[] { baseUrl });
		Map<String, Object> metaMap = new HashMap<>();
		List<String> styleSheets = new ArrayList<>();
		try (FileSystem fileSystem = FileSystems.newFileSystem(bundleURI, Collections.emptyMap())) {
			Path meta = fileSystem.getPath("/" + Theme.PROPERTY_FILE_NAME);
			metaMap.putAll(GsonUtils.fromJson(meta, metaMap.getClass()));
			metaMap.put(Theme.INTERNAL_PROPERTY_KEY_PACKAGE_PATH, baseUrl.getFile());
			Files.walkFileTree(fileSystem.getPath("/"), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
					styleSheets.add(path.toString());
					return super.visitFile(path, attrs);
				}
			});
		}
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
	}
}
