package org.to2mbn.lolixl.main;

import static java.lang.String.format;
import static java.util.stream.Collectors.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;
import org.apache.felix.framework.Felix;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

class InternalBundleRepository {

	private static final Logger LOGGER = Logger.getLogger(InternalBundleRepository.class.getCanonicalName());

	private static final String PATH_ARTIFACTS_LIST = "org.to2mbn.lolixl.systemBundles.list";
	private static final String PATH_BOOTSTRAP_ARTIFACTS_LIST = "org.to2mbn.lolixl.bootstrapBundles.list";
	private static final String PATH_BUNDLES_ROOT = "bundles";

	private Path jarRoot;
	private Path repoRoot;
	private Set<String> artifacts;
	private Set<String> bootstrapBundles;
	private Path localRepo = new File(".lolixl/m2/repo").toPath();
	private Map<String, String> ga2v;
	private Map<String, Set<String>> ga2names;
	private Map<String, Bundle> gav2bootstrapBundles;
	private Map<String, byte[]> gav2bootstrapBundlesData;

	public InternalBundleRepository() throws URISyntaxException, IOException {
		URI uriToLookup = InternalBundleRepository.class.getResource("/" + PATH_BOOTSTRAP_ARTIFACTS_LIST).toURI();
		try {
			trySetupZipFileSystem(uriToLookup);
		} catch (FileSystemNotFoundException ex) {
			String[] splited = uriToLookup.toString().split("!", 2);
			jarRoot = FileSystems.newFileSystem(new URI(splited[0]), Collections.emptyMap()).getPath("/");
		}

		repoRoot = jarRoot.resolve(PATH_BUNDLES_ROOT);
		artifacts = readArtifacts(jarRoot.resolve(PATH_ARTIFACTS_LIST));
		bootstrapBundles = readArtifacts(jarRoot.resolve(PATH_BOOTSTRAP_ARTIFACTS_LIST));
		resolveGA2V();
	}

	private void trySetupZipFileSystem(URI uriToLookup) throws URISyntaxException {
		LOGGER.fine("Try locating " + uriToLookup);
		jarRoot = Paths.get(uriToLookup).getParent();
	}

	private Set<String> readArtifacts(Path listFile) throws IOException {
		return Files.lines(listFile, Charset.forName("UTF-8"))
				.filter(s -> !s.trim().isEmpty())
				.filter(s -> !s.startsWith("#"))
				.collect(toCollection(LinkedHashSet::new));
	}

	private void resolveGA2V() throws IOException {
		Map<String, String> prefix2ga = new TreeMap<>(
				Comparator.comparing(String::length)
						.reversed()
						.thenComparing(s -> s));
		artifacts.forEach(gastr -> {
			String[] ga = gastr.split(":");
			if (ga.length != 2)
				throw new IllegalArgumentException("Illegal GA: " + gastr);
			prefix2ga.put(ga[0] + "." + ga[1], gastr);
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
				.collect(groupingBy(o -> o[0], mapping(o -> o[1], toCollection(TreeSet::new))));
		ga2v = new HashMap<>();
		ga2names.forEach((ga, names) -> {
			String artifactNameToInfer = names.stream()
					.sorted(Comparator.comparing((String s) -> s.length())
							.thenComparing(s -> s))
					.findFirst()
					.get();
			String version = artifactNameToInfer.substring(ga.length() + 1, artifactNameToInfer.lastIndexOf('.'));
			ga2v.put(ga, version);
			LOGGER.fine(format("Found GAV mapping: %s -> %s", ga, version));
		});
	}

	public FileChannel openChannel(String groupId, String artifactId, String version, String classifier, String type) throws IOException {
		LOGGER.fine(format("Try opening channel groupId=[%s], artifactId=[%s], version=[%s], classifier=[%s], type=[%s]", groupId, artifactId, version, classifier, type));
		if (!artifacts.contains(groupId + ":" + artifactId)) {
			return null;
		}
		Path p = repoRoot.resolve(groupId + "." + artifactId + "-" + version + (classifier == null ? "" : "-" + classifier) + "." + type);
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
						.resolve(name.substring(g.length() + 1));
				Files.createDirectories(target.getParent());
				Files.copy(src, target, StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}

	public void init(Felix felix) throws IOException, BundleException {
		BundleContext ctx = felix.getBundleContext();
		copyBootstrapArtifacts();
		Set<Bundle> bundles = new LinkedHashSet<>();
		gav2bootstrapBundles = new LinkedHashMap<>();
		gav2bootstrapBundlesData = new LinkedHashMap<>();
		for (String ga : bootstrapBundles) {
			int idxMaohao = ga.indexOf(':');
			String g = ga.substring(0, idxMaohao);
			String a = ga.substring(idxMaohao + 1);
			String v = ga2v.get(ga);
			String gav = g + ":" + a + ":" + v;
			LOGGER.info("Installing bootstrap bundle " + gav);

			Path localRepoArtifact = getLocalArtifactPath(g, a, v, null, "jar");

			Bundle bundle;
			if ("true".equals(System.getProperty("lolixl.readPluginToMem"))) {
				String uri = "lolixl:bundles/" + g + ":" + a;
				ByteArrayOutputStream buf;
				try (FileChannel channel = FileChannel.open(localRepoArtifact)) {
					buf = new ByteArrayOutputStream((int) channel.size());
					WritableByteChannel out = Channels.newChannel(buf);
					channel.transferTo(0, channel.size(), out);
				}
				byte[] data = buf.toByteArray();
				gav2bootstrapBundlesData.put(gav, data);
				bundle = ctx.installBundle(uri, new ByteArrayInputStream(data));
			} else {
				bundle = ctx.installBundle(localRepoArtifact.toUri().toString());
			}

			bundles.add(bundle);
			gav2bootstrapBundles.put(gav, bundle);
		}
		for (Bundle bundle : bundles) {
			bundle.start();
		}
	}

	//

	private Path getLocalArtifactPath(String groupId, String artifactId, String version, String classifier, String type) {
		Path p = localRepo;
		for (String gid : groupId.split("\\."))
			p = p.resolve(gid);
		p = p.resolve(artifactId)
				.resolve(version)
				.resolve(getArtifactFileName(artifactId, version, classifier, type));
		return p;
	}

	private static String getArtifactFileName(String artifactId, String version, String classifier, String type) {
		StringBuilder sb = new StringBuilder();
		sb.append(artifactId)
				.append('-')
				.append(version);
		if (classifier != null) {
			sb.append('-')
					.append(classifier);
		}
		sb.append('.')
				.append(type == null ? "jar" : type);
		return sb.toString();
	}

}
