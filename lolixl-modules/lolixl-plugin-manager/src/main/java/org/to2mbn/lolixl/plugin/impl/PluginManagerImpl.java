package org.to2mbn.lolixl.plugin.impl;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.service.component.ComponentContext;
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
import org.to2mbn.lolixl.utils.BundleUtils;
import org.to2mbn.lolixl.utils.FXUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

@Service({ PluginManager.class })
@Component
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
	private BundleListener bundleListener = event -> {
		int type = event.getType();
		if (type == BundleEvent.STARTED || type == BundleEvent.STOPPED)
			updateEnabledPlugins();
	};

	private Map<MavenArtifact, CompletableFuture<Void>> inProgressDownloads = new HashMap<>();

	private volatile ObservableList<Plugin> enabledPlugins;
	private volatile ObservableList<Plugin> enabledPluginsView;
	private Object enabledPluginsLock = new Object();

	@Activate
	public void active(ComponentContext compCtx) {
		compCtx.getBundleContext().addBundleListener(bundleListener);
	}

	@Deactivate
	public void deactive(ComponentContext compCtx) {
		compCtx.getBundleContext().removeBundleListener(bundleListener);
	}

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
								.thenApply(description2 -> {
									if (description2.isPresent())
										return description2.get();
									else
										throw AsyncUtils.wrapWithCompletionException(new ArtifactNotFoundException(artifact.toString()));
								});
				})
				.thenCompose(description -> {
					Set<MavenArtifact> artifacts = new HashSet<>(description.getDependencies());
					artifacts.add(description.getArtifact());
					return CompletableFuture.allOf(artifacts.stream()
							.map(this::downloadArtifact)
							.toArray(CompletableFuture[]::new));
				})
				.thenCompose(dummy -> pluginService.loadPlugin(artifact));
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

	private CompletableFuture<Void> downloadArtifact(MavenArtifact artifact) {
		synchronized (inProgressDownloads) {
			CompletableFuture<Void> future = inProgressDownloads.get(artifact);
			if (future == null) {
				future = localPluginRepo.downloadBundle(remotePluginRepo, artifact)
						.whenComplete((result, ex) -> {
							synchronized (inProgressDownloads) {
								inProgressDownloads.remove(artifact);
							}
						});
				inProgressDownloads.put(artifact, future);
				if (future.isCancelled() || future.isCompletedExceptionally() || future.isDone()) { // check again
					inProgressDownloads.remove(artifact);
				}
			}
			return future;
		}
	}

	@Override
	public ObservableList<Plugin> enabledPluginsList() {
		if (enabledPluginsView != null) {
			return enabledPluginsView;
		}
		if (!FXUtils.isFxInitialized()) {
			throw new IllegalStateException("JavaFX not initialized");
		}
		synchronized (enabledPluginsLock) {
			if (enabledPlugins == null) {
				enabledPlugins = FXCollections.observableArrayList();
				enabledPluginsView = FXCollections.unmodifiableObservableList(enabledPlugins);
			}
			updateEnabledPlugins();
		}
		return enabledPluginsView;
	}

	private void updateEnabledPlugins() {
		Set<Plugin> sorted = new TreeSet<>();
		for (Plugin plugin : pluginService.getLoadedPlugins()) {
			BundleUtils.waitBundleStarted(plugin.getBundle());
			if (plugin.getBundle().getState() == Bundle.ACTIVE) {
				sorted.add(plugin);
			}
		}
		Platform.runLater(() -> enabledPlugins.setAll(sorted));
	}

}
