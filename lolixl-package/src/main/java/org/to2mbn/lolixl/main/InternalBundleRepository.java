package org.to2mbn.lolixl.main;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.CodeSource;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.felix.framework.Felix;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

class InternalBundleRepository {

	private static final Logger LOGGER = Logger.getLogger(InternalBundleRepository.class.getCanonicalName());

	private static final String PATH_ARTIFACTS_LIST = "org.to2mbn.lolixl.systemBundles.list";
	private static final String PATH_BOOTSTRAP_ARTIFACTS_LIST = "org.to2mbn.lolixl.bootstrapBundles.list";
	private static final String PATH_BUNDLES_ROOT = "bundles";

	private Path repoRoot;
	private Set<String> artifacts;
	private Set<String> bootstrapBundles;
	private Path localRepo = new File(".lolixl/m2/repo").toPath();

	public InternalBundleRepository() throws URISyntaxException, IOException {
		CodeSource codeSource = InternalBundleRepository.class.getProtectionDomain().getCodeSource();
		if (codeSource == null) {
			throw new IllegalStateException("codeSource is null");
		}
		URL url = codeSource.getLocation();
		if (url == null) {
			throw new IllegalStateException("codeSource.url is null");
		}
		Path jarRoot = Paths.get(url.toURI());
		repoRoot = jarRoot.resolve(PATH_BUNDLES_ROOT);
		artifacts = readArtifacts(jarRoot.resolve(PATH_ARTIFACTS_LIST));
		bootstrapBundles = readArtifacts(jarRoot.resolve(PATH_BOOTSTRAP_ARTIFACTS_LIST));

	}

	private Set<String> readArtifacts(Path listFile) throws IOException {
		return Files.lines(listFile, Charset.forName("UTF-8"))
				.filter(s -> !s.trim().isEmpty())
				.filter(s -> !s.startsWith("#"))
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	public FileChannel openChannel(String groupId, String artifactId, String version, String classifier, String type) throws IOException {
		if (!artifacts.contains(groupId + ":" + artifactId)) {
			return null;
		}
		Path p = repoRoot.resolve(artifactId + "-" + version + (classifier == null ? "" : "-" + classifier) + "." + type);
		if (!Files.exists(p)) {
			return null;
		}
		return FileChannel.open(p, StandardOpenOption.READ);
	}

	/**
	 * @return ga2v
	 * @throws IOException when an io error occurs
	 */
	private Map<String, String> copyBootstrapArtifacts() throws IOException {
		Map<String, String> prefix2ga = new HashMap<>();
		bootstrapBundles.forEach(gastr -> {
			String[] ga = gastr.split(":");
			if (ga.length != 2)
				throw new IllegalArgumentException("Illegal GA: " + gastr);
			prefix2ga.put(ga[1], gastr);
		});

		Map<String, Set<String>> ga2names = Files.list(repoRoot)
				.map(src -> {
					String name = src.getFileName().toString();
					String ga = null;
					for (Entry<String, String> entry : prefix2ga.entrySet())
						if (name.startsWith(entry.getKey())) {
							ga = entry.getValue();
							break;
						}
					if (ga == null)
						return null;
					String[] o = new String[2];
					o[0] = ga;
					o[1] = name;
					return o;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.groupingBy(o -> o[0], Collectors.mapping(o -> o[1], Collectors.toCollection(TreeSet::new))));

		Map<String, String> ga2v = new HashMap<>();

		ga2names.forEach((ga, names) -> {
			String artifactNameToInfer = names.stream()
					.sorted(Comparator.comparing((String s) -> s.length())
							.thenComparing(s -> s))
					.findFirst()
					.get();
			int idxMaohao = ga.indexOf(':');
			String version = artifactNameToInfer.substring(ga.length() - idxMaohao, artifactNameToInfer.lastIndexOf('.'));
			ga2v.put(ga, version);
			names.forEach(name -> {
				Path src = repoRoot.resolve(name);
				Path target = localRepo;
				for (String g : ga.substring(0, idxMaohao).split("\\."))
					target = target.resolve(g);
				target = target.resolve(ga.substring(idxMaohao + 1))
						.resolve(version)
						.resolve(name);
				try {
					Files.createDirectories(target.getParent());
					Files.copy(src, target);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
		});
		return ga2v;
	}

	public void init(Felix felix) throws IOException, BundleException {
		BundleContext ctx = felix.getBundleContext();
		Map<String, String> ga2v = copyBootstrapArtifacts();
		Set<Bundle> bundles = new LinkedHashSet<>();
		for (String ga : bootstrapBundles) {
			int idxMaohao = ga.indexOf(':');
			String g = ga.substring(0, idxMaohao);
			String a = ga.substring(idxMaohao + 1);
			String v = ga2v.get(ga);
			String uri = "lolixl:///localm2/" + g + ":" + a + ":" + v;
			LOGGER.info("Installing bootstrap bundle " + uri);
			bundles.add(ctx.installBundle(uri, Channels.newInputStream(openChannel(g, a, v, null, "jar"))));
		}
		for (Bundle bundle : bundles)
			bundle.start();
	}

}
