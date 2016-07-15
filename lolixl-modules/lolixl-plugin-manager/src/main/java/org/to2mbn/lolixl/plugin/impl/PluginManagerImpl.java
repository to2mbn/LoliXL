package org.to2mbn.lolixl.plugin.impl;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Reference;
import org.to2mbn.lolixl.plugin.LocalPluginRepository;
import org.to2mbn.lolixl.plugin.Plugin;
import org.to2mbn.lolixl.plugin.PluginManager;
import org.to2mbn.lolixl.plugin.PluginRepository;
import org.to2mbn.lolixl.plugin.PluginService;
import org.to2mbn.lolixl.plugin.impl.resolver.DependencyResolver;
import org.to2mbn.lolixl.plugin.impl.resolver.VersionComparator;
import org.to2mbn.lolixl.plugin.maven.ArtifactNotFoundException;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;
import org.to2mbn.lolixl.utils.AsyncUtils;

public class PluginManagerImpl implements PluginManager {

	private static final Logger LOGGER = Logger.getLogger(PluginManagerImpl.class.getCanonicalName());

	@Reference
	private PluginService pluginService;

	@Reference(target = "(pluginRepo.type=local)")
	private LocalPluginRepository localPluginRepo;

	@Reference(target = "(pluginRepo.type=remote)")
	private PluginRepository remotePluginRepo;

	@Reference(target = "(usage=local_io)")
	private ExecutorService localIOPool;

	private Comparator<String> versionComparator = new VersionComparator();

	@Override
	public PluginService getService() {
		return pluginService;
	}

	@Override
	public LocalPluginRepository getLocalRepository() {
		return localPluginRepo;
	}

	@Override
	public PluginRepository getRemoteRepository() {
		return remotePluginRepo;
	}

	@Override
	public CompletableFuture<Plugin> install(MavenArtifact artifact) {
		Objects.requireNonNull(artifact);
		return localPluginRepo.getPluginDescription(artifact)
				.thenCompose(description -> {
					if (description.isPresent())
						return CompletableFuture.completedFuture(description.get());
					else
						return remotePluginRepo.getPluginDescription(artifact)
								.thenApply(description2 -> description2.orElseThrow(() -> AsyncUtils.wrapWithCompletionException(new ArtifactNotFoundException(artifact.toString()))));
				})
				.thenCompose(description -> {
					Set<MavenArtifact> artifacts = new HashSet<>(description.getDependencies());
					artifacts.add(description.getArtifact());
					return CompletableFuture.allOf(artifacts.stream()
							.map(dependency -> localPluginRepo.downloadBundle(remotePluginRepo, dependency))
							.toArray(CompletableFuture[]::new));
				})
				.thenCompose(dummy -> pluginService.loadPlugin(artifact));
	}

	@Override
	public CompletableFuture<Void> uninstall(Plugin plugin) {
		return plugin.unload()
				.thenCompose(dummy -> cleanup());
	}

	@Override
	public CompletableFuture<Optional<MavenArtifact>> checkForUpdate(MavenArtifact artifact) {
		return remotePluginRepo.getRepository().getVersioning(artifact.getGroupId(), artifact.getArtifactId())
				.thenApply(versioning -> {
					String remote = versioning.getLatest();
					String current = artifact.getVersion();
					if (versionComparator.compare(remote, current) > 0) {
						return Optional.of(new MavenArtifact(artifact.getGroupId(), artifact.getArtifactId(), remote));
					} else {
						return Optional.empty();
					}
				});
	}

	@Override
	public CompletableFuture<Void> cleanup() {
		return AsyncUtils.asyncRun(() -> {
			Map<String, MavenArtifact> ga2usingArtifact = DependencyResolver.toGAMapping(pluginService.getLoadedArtifacts());
			localPluginRepo.getRepository().listArtifacts()
					.filter(artifact -> {
						MavenArtifact sameGAInUse = ga2usingArtifact.get(artifact.getGroupId() + ":" + artifact.getArtifactId());
						return sameGAInUse == null ||
								versionComparator.compare(artifact.getVersion(), sameGAInUse.getVersion()) < 0;
					})
					.forEach(artifact -> {
						LOGGER.info("Deleting " + artifact);
						localPluginRepo.getRepository().deleteArtifact(artifact);
					});
			return null;
		}, localIOPool);
	}

}
