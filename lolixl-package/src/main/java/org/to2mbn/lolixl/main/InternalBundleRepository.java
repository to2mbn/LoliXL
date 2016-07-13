package org.to2mbn.lolixl.main;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.CodeSource;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
	private Map<String, String> ga2v;
	private Map<String, Set<String>> ga2names;
	private Map<String, Bundle> gav2bootstrapBundles;
	private Map<String, byte[]> gav2bootstrapBundlesData;

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
		resolveGA2V();
	}

	private Set<String> readArtifacts(Path listFile) throws IOException {
		return Files.lines(listFile, Charset.forName("UTF-8"))
				.filter(s -> !s.trim().isEmpty())
				.filter(s -> !s.startsWith("#"))
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	private void resolveGA2V() throws IOException {
		Map<String, String> prefix2ga = new HashMap<>();
		artifacts.forEach(gastr -> {
			String[] ga = gastr.split(":");
			if (ga.length != 2)
				throw new IllegalArgumentException("Illegal GA: " + gastr);
			prefix2ga.put(ga[1], gastr);
		});
		ga2names = Files.list(repoRoot)
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
		ga2v = new HashMap<>();
		ga2names.forEach((ga, names) -> {
			String artifactNameToInfer = names.stream()
					.sorted(Comparator.comparing((String s) -> s.length())
							.thenComparing(s -> s))
					.findFirst()
					.get();
			String version = artifactNameToInfer.substring(ga.length() - ga.indexOf(':'), artifactNameToInfer.lastIndexOf('.'));
			ga2v.put(ga, version);
		});
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

	public String getVersion(String groupId, String artifactId) {
		return ga2v.get(groupId + ":" + artifactId);
	}

	public Map<String, Bundle> getGav2bootstrapBundles() {
		return gav2bootstrapBundles;
	}

	public byte[] getBootstrapBundleData(String gav) {
		return gav2bootstrapBundlesData.get(gav);
	}

	private void copyBootstrapArtifacts() throws IOException {
		for (String ga : bootstrapBundles) {
			int idxMaohao = ga.indexOf(':');
			String g = ga.substring(0, idxMaohao);
			String a = ga.substring(idxMaohao + 1);
			String v = ga2v.get(ga);
			for (String name : ga2names.get(ga)) {
				Path src = repoRoot.resolve(name);
				Path target = localRepo;
				for (String sg : g.split("\\."))
					target = target.resolve(sg);
				target = target.resolve(a)
						.resolve(v)
						.resolve(name);
				try {
					Files.createDirectories(target.getParent());
					Files.copy(src, target, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}
		}
	}

	public void init(Felix felix) throws IOException, BundleException {
		BundleContext ctx = felix.getBundleContext();
		copyBootstrapArtifacts();
		Set<Bundle> bundles = new LinkedHashSet<>();
		gav2bootstrapBundles = new LinkedHashMap<>();
		for (String ga : bootstrapBundles) {
			int idxMaohao = ga.indexOf(':');
			String g = ga.substring(0, idxMaohao);
			String a = ga.substring(idxMaohao + 1);
			String v = ga2v.get(ga);
			String gav = g + ":" + a + ":" + v;
			String uri = "lolixl:///bundles/" + gav;
			LOGGER.info("Installing bootstrap bundle " + uri);
			ByteArrayOutputStream buf;
			try (FileChannel channel = openChannel(g, a, v, null, "jar")) {
				buf = new ByteArrayOutputStream((int) channel.size());
				WritableByteChannel out = Channels.newChannel(buf);
				channel.transferTo(0, channel.size(), out);
			}
			byte[] data = buf.toByteArray();
			gav2bootstrapBundlesData.put(gav, data);
			Bundle bundle = ctx.installBundle(uri, new ByteArrayInputStream(data));
			bundles.add(bundle);
			gav2bootstrapBundles.put(gav, bundle);
		}
		for (Bundle bundle : bundles) {
			bundle.start();
		}
	}

}
