package org.to2mbn.lolixl.plugin.impl.resolver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;
import org.to2mbn.lolixl.plugin.PluginDescription;
import org.to2mbn.lolixl.plugin.impl.resolver.DependencyAction.InstallAction;
import org.to2mbn.lolixl.plugin.impl.resolver.DependencyAction.UpdateAction;
import org.to2mbn.lolixl.plugin.impl.resolver.DependencyAction.UninstallAction;
import static java.util.stream.Collectors.*;

public class DependencyResolver {

	private Comparator<String> reversedVersionComparator = new VersionComparator().reversed();

	public Set<MavenArtifact> computeState(Set<PluginDescription> plugins) {
		Set<MavenArtifact> artifacts = new LinkedHashSet<>();
		plugins.forEach(description -> {
			artifacts.add(description.getArtifact());
			artifacts.addAll(description.getDependencies());
		});
		return artifacts.stream()
				.collect(groupingBy(artifact -> artifact.getGroupId() + ":" + artifact.getArtifactId(),
						LinkedHashMap::new,
						mapping(artifact -> artifact.getVersion(),
								toCollection(() -> new TreeSet<>(reversedVersionComparator)))))
				.entrySet().stream()
				.map(entry -> {
					String[] splitedGA = entry.getKey().split(":", 2);
					String version = entry.getValue().first();
					return new MavenArtifact(splitedGA[0], splitedGA[1], version);
				})
				.collect(toCollection(LinkedHashSet::new));
	}

	public List<DependencyAction> transferState(Set<MavenArtifact> from, Set<MavenArtifact> to) {
		Map<String, MavenArtifact> gaMappingFrom = toGAMapping(from);
		Map<String, MavenArtifact> gaMappingTo = toGAMapping(to);
		List<DependencyAction> actions = new ArrayList<>();
		
		// Install
		Set<String> gaToInstall = new LinkedHashSet<>(gaMappingTo.keySet());
		gaToInstall.removeAll(gaMappingFrom.keySet());
		for (String ga : gaToInstall)
			actions.add(new InstallAction(gaMappingTo.get(ga)));

		// Update
		Set<String> gaToCheckUpdate = new LinkedHashSet<>(gaMappingTo.keySet());
		gaToCheckUpdate.retainAll(gaMappingFrom.keySet());
		for (String ga : gaToCheckUpdate) {
			MavenArtifact src = gaMappingFrom.get(ga);
			MavenArtifact dest = gaMappingTo.get(ga);
			if (!src.getVersion().equals(dest.getVersion())) {
				actions.add(new UpdateAction(src, dest));
			}
		}

		// Uninstall
		Set<String> gaToUninstall = new LinkedHashSet<>(gaMappingFrom.keySet());
		gaToUninstall.removeAll(gaMappingTo.keySet());
		for (String ga : gaToUninstall)
			actions.add(new UninstallAction(gaMappingFrom.get(ga)));

		return actions;
	}

	private Map<String, MavenArtifact> toGAMapping(Set<MavenArtifact> artifacts) {
		Map<String, MavenArtifact> gaMapping = new LinkedHashMap<>();
		for (MavenArtifact artifact : artifacts)
			gaMapping.put(artifact.getGroupId() + ":" + artifact.getArtifactId(), artifact);
		return gaMapping;
	}

}
