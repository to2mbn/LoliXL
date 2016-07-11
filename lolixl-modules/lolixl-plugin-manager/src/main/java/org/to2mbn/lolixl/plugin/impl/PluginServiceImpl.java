package org.to2mbn.lolixl.plugin.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
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

@Service({ PluginService.class })
@Component(immediate = true)
public class PluginServiceImpl implements PluginService {

	private static final Logger LOGGER = Logger.getLogger(PluginServiceImpl.class.getCanonicalName());

	@Reference(target = "(m2repository.type=local)")
	private LocalMavenRepository localM2Repo;

	@Reference(target = "(pluginRepo.type=local)")
	private LocalPluginRepository localPluginRepo;

	private Map<MavenArtifact, Bundle> artifact2bundle = new ConcurrentHashMap<>();
	private Map<Bundle, Plugin> bundle2plugin = new ConcurrentHashMap<>();
	private Set<Plugin> loadedPlugins = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private Set<PluginDescription> currentState = Collections.newSetFromMap(new ConcurrentHashMap<>());

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
				artifact2bundle.put(artifact, bundle);
				descriptionFutures.put(bundle, localPluginRepo.getPluginDescription(artifact));
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Plugin> getPlugin(Bundle bundle) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Plugin> loadPlugin(LocalMavenRepository repository, MavenArtifact artifact) {
		// TODO Auto-generated method stub
		return null;
	}

	private void addPlugin0(Bundle bundle, PluginDescription description) {
		PluginImpl plugin = new PluginImpl();
		plugin.bundle = bundle;
		plugin.description = description;
		plugin.container = this;
		bundle2plugin.put(bundle, plugin);
		loadedPlugins.add(plugin);
		currentState.add(description);
	}

	private void checkDependenciesState() throws IllegalStateException {
		Set<MavenArtifact> current = artifact2bundle.keySet();
		Set<MavenArtifact> expected = resolver.computeState(currentState);
		if (!current.equals(expected)) {
			throw new IllegalStateException(String.format("Illegal dependencies state\ncurrent: %s\nexpected:%s", current, expected));
		}
	}

}
