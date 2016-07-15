package org.to2mbn.lolixl.plugin.impl.resolver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;
import org.to2mbn.lolixl.plugin.DependencyAction;
import org.to2mbn.lolixl.plugin.PluginDescription;
import org.to2mbn.lolixl.plugin.DependencyAction.InstallAction;
import org.to2mbn.lolixl.plugin.DependencyAction.UninstallAction;
import org.to2mbn.lolixl.plugin.DependencyAction.UpdateAction;
import static java.util.stream.Collectors.*;

public final class DependencyResolver {

	private DependencyResolver() {}

	private static Comparator<String> reversedVersionComparator = new VersionComparator().reversed();

	public static Map<MavenArtifact, PluginDescription> toArtifact2DescriptionMap(Set<PluginDescription> descriptions) {
		return descriptions.stream()
				.collect(groupingBy(PluginDescription::getArtifact, collectingAndThen(toList(), (List<PluginDescription> set) -> {
					if (set.size() == 1)
						return set.get(0);
					else
						throw new IllegalStateException("Illegal collecting result:" + set);
				})));
	}

	public static Set<MavenArtifact> computeState(Set<PluginDescription> plugins) {
		Set<MavenArtifact> artifacts = new HashSet<>();
		plugins.forEach(description -> {
			artifacts.add(description.getArtifact());
			artifacts.addAll(description.getDependencies());
		});
		return merge(artifacts);
	}

	public static Set<MavenArtifact> merge(Set<MavenArtifact> artifacts) {
		return artifacts.stream()
				.collect(groupingBy(artifact -> artifact.getGroupId() + ":" + artifact.getArtifactId(),
						mapping(artifact -> artifact.getVersion(),
								toCollection(() -> new TreeSet<>(reversedVersionComparator)))))
				.entrySet().stream()
				.map(entry -> {
					String[] splitedGA = entry.getKey().split(":", 2);
					String version = entry.getValue().first();
					return new MavenArtifact(splitedGA[0], splitedGA[1], version);
				})
				.collect(toSet());
	}

	public static List<DependencyAction> transferState(Set<MavenArtifact> from, Set<MavenArtifact> to) {
		Map<String, MavenArtifact> gaMappingFrom = toGAMapping(from);
		Map<String, MavenArtifact> gaMappingTo = toGAMapping(to);
		List<DependencyAction> actions = new ArrayList<>();

		// Install
		Set<String> gaToInstall = new HashSet<>(gaMappingTo.keySet());
		gaToInstall.removeAll(gaMappingFrom.keySet());
		for (String ga : gaToInstall)
			actions.add(new InstallAction(gaMappingTo.get(ga)));

		// Update
		Set<String> gaToCheckUpdate = new HashSet<>(gaMappingTo.keySet());
		gaToCheckUpdate.retainAll(gaMappingFrom.keySet());
		for (String ga : gaToCheckUpdate) {
			MavenArtifact src = gaMappingFrom.get(ga);
			MavenArtifact dest = gaMappingTo.get(ga);
			if (!src.getVersion().equals(dest.getVersion())) {
				actions.add(new UpdateAction(src, dest));
			}
		}

		// Uninstall
		Set<String> gaToUninstall = new HashSet<>(gaMappingFrom.keySet());
		gaToUninstall.removeAll(gaMappingTo.keySet());
		for (String ga : gaToUninstall)
			actions.add(new UninstallAction(gaMappingFrom.get(ga)));

		return actions;
	}

	public static Map<String, MavenArtifact> toGAMapping(Set<MavenArtifact> artifacts) {
		Map<String, MavenArtifact> gaMapping = new HashMap<>();
		for (MavenArtifact artifact : artifacts)
			gaMapping.put(artifact.getGroupId() + ":" + artifact.getArtifactId(), artifact);
		return gaMapping;
	}

}
