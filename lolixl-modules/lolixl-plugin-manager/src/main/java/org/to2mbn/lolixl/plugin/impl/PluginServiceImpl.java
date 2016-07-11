package org.to2mbn.lolixl.plugin.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Bundle;
import org.to2mbn.lolixl.plugin.LocalPluginRepository;
import org.to2mbn.lolixl.plugin.Plugin;
import org.to2mbn.lolixl.plugin.PluginDescription;
import org.to2mbn.lolixl.plugin.PluginService;
import org.to2mbn.lolixl.plugin.impl.resolver.DependencyResolver;
import org.to2mbn.lolixl.plugin.maven.LocalMavenRepository;
import org.to2mbn.lolixl.plugin.maven.MavenArtifact;
import static java.lang.String.format;
import static java.util.stream.Collectors.*;

@Service({ PluginService.class })
@Component(immediate = true)
public class PluginServiceImpl implements PluginService {

	private static final Logger LOGGER = Logger.getLogger(PluginServiceImpl.class.getCanonicalName());

	@Reference(target = "(m2repository.type=local)")
	private LocalMavenRepository localM2Repo;

	@Reference(target = "(pluginRepo.type=local)")
	private LocalPluginRepository localPluginRepo;

	private Map<MavenArtifact, Bundle> artifact2bundle = new ConcurrentHashMap<>();
	private Map<Bundle, MavenArtifact> bundle2artifact = new ConcurrentHashMap<>();
	private Map<Bundle, Plugin> bundle2plugin = new ConcurrentHashMap<>();
	private Set<Plugin> loadedPlugins = Collections.newSetFromMap(new ConcurrentHashMap<>());

	private Set<Plugin> loadedPluginsView = Collections.unmodifiableSet(loadedPlugins);
	private DependencyResolver resolver = new DependencyResolver();
	private final Object lock = new Object();

	@Activate
	public void active() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, InterruptedException, ExecutionException {
		Class<?> endpoint = Class.forName("org.to2mbn.lolixl.main.AccessEndpoint", true, ClassLoader.getSystemClassLoader());
		@SuppressWarnings("unchecked")
		Map<String, Bundle> gav2bootstrapBundles = (Map<String, Bundle>) endpoint.getDeclaredMethod("getGav2bootstrapBundles").invoke(null);
		Map<Bundle, CompletableFuture<Optional<PluginDescription>>> descriptionFutures = new HashMap<>();
		synchronized (lock) {
			for (Entry<String, Bundle> entry : gav2bootstrapBundles.entrySet()) {
				String gav = entry.getKey();
				Bundle bundle = entry.getValue();
				String[] splitedGAV = gav.split(":", 3);
				MavenArtifact artifact = new MavenArtifact(splitedGAV[0], splitedGAV[1], splitedGAV[2]);
				descriptionFutures.put(bundle, localPluginRepo.getPluginDescription(artifact));
				addBundle0(bundle, artifact);
			}
			for (Entry<Bundle, CompletableFuture<Optional<PluginDescription>>> entry : descriptionFutures.entrySet()) {
				Bundle bundle = entry.getKey();
				Optional<PluginDescription> optionalDescription = entry.getValue().get();
				if (!optionalDescription.isPresent())
					continue;
				PluginDescription description = optionalDescription.get();
				addPlugin0(bundle, description);
			}
			checkDependenciesState();
		}
		LOGGER.info("Plugin service initialized");
	}

	@Override
	public Set<Plugin> getLoadedPlugins() {
		return loadedPluginsView;
	}

	@Override
	public Optional<Plugin> getPlugin(MavenArtifact artifact) {
		Bundle bundle = artifact2bundle.get(artifact);
		if (bundle != null)
			return Optional.ofNullable(bundle2plugin.get(bundle));
		return Optional.empty();
	}

	@Override
	public Optional<Plugin> getPlugin(Bundle bundle) {
		return Optional.ofNullable(bundle2plugin.get(bundle));
	}

	@Override
	public Optional<Bundle> getBundle(MavenArtifact artifact) {
		return Optional.ofNullable(artifact2bundle.get(artifact));
	}

	@Override
	public Optional<MavenArtifact> getArtifact(Bundle bundle) {
		return Optional.ofNullable(bundle2artifact.get(bundle));
	}

	@Override
	public CompletableFuture<Plugin> loadPlugin(LocalMavenRepository repository, MavenArtifact artifact) {
		// TODO Auto-generated method stub
		return null;
	}

	CompletableFuture<Void> unloadPlugin(Plugin plugin) {
		// TODO Auto-generated method stub
		return null;
	}

	private void addBundle0(Bundle bundle, MavenArtifact artifact) {
		artifact2bundle.put(artifact, bundle);
		bundle2artifact.put(bundle, artifact);

	}

	private void addPlugin0(Bundle bundle, PluginDescription description) {
		PluginImpl plugin = new PluginImpl();
		plugin.bundle = bundle;
		plugin.description = description;
		plugin.container = this;

		bundle2plugin.put(bundle, plugin);
		loadedPlugins.add(plugin);
	}

	private void checkDependenciesState() throws IllegalStateException {
		if (artifact2bundle.size() != bundle2artifact.size())
			throw new IllegalStateException(format("artifact2bundle(%d) & bundle2artifact(%d) sizes mismatch", artifact2bundle.size(), bundle2artifact.size()));
		artifact2bundle.forEach((artifact, bundle) -> {
			if (bundle2artifact.get(bundle) != artifact)
				throw new IllegalStateException(format("Missing/wrong bundle2artifact mapping: %s -> %s", bundle, artifact));
		});
		if (!new HashSet<>(bundle2plugin.values()).equals(loadedPlugins))
			throw new IllegalStateException(format("bundle2plugin & loadedPlugins mismatch\nbundle2plugin: %s\nloadedPlugins: %s", bundle2plugin, loadedPlugins));
		Set<Bundle> pluginBundleCheck = new HashSet<>(bundle2plugin.keySet());
		pluginBundleCheck.removeAll(bundle2artifact.keySet());
		if (!pluginBundleCheck.isEmpty())
			throw new IllegalStateException(format("%s are in bundle2plugin, but not in bundle2artifact", pluginBundleCheck));

		Set<MavenArtifact> current = artifact2bundle.keySet();
		Set<MavenArtifact> expected = resolver.computeState(loadedPlugins.stream()
				.map(Plugin::getDescription)
				.collect(toSet()));
		if (!current.equals(expected))
			throw new IllegalStateException(format("Illegal dependencies state\ncurrent: %s\nexpected:%s", current, expected));
	}

}
