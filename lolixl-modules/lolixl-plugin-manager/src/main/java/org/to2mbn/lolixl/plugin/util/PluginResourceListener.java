package org.to2mbn.lolixl.plugin.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.to2mbn.lolixl.plugin.Plugin;
import org.to2mbn.lolixl.plugin.PluginManager;
import org.to2mbn.lolixl.utils.CollectionUtils;
import org.to2mbn.lolixl.utils.GsonUtils;
import javafx.beans.InvalidationListener;

public class PluginResourceListener<T> {

	private static final Logger LOGGER = Logger.getLogger(PluginResourceListener.class.getCanonicalName());

	public static <T> PluginResourceListener<T> json(String resource, Type type) {
		return new PluginResourceListener<>(resource, in -> {
			try {
				return GsonUtils.instance().fromJson(new InputStreamReader(in, "UTF-8"), type);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	private String resource;
	private Function<InputStream, T> mapper;

	private InvalidationListener listener;

	private Map<Plugin, T> mapping = new ConcurrentHashMap<>();
	private List<BiConsumer<Plugin, T>> addActions = new Vector<>();
	private List<BiConsumer<Plugin, T>> removeActions = new Vector<>();

	public PluginResourceListener(String resource, Function<InputStream, T> mapper) {
		this.resource = Objects.requireNonNull(resource);
		this.mapper = Objects.requireNonNull(mapper);
	}

	public PluginResourceListener<T> whenAdding(BiConsumer<Plugin, T> action) {
		addActions.add(action);
		return this;
	}

	public PluginResourceListener<T> whenRemoving(BiConsumer<Plugin, T> action) {
		removeActions.add(action);
		return this;
	}

	public Map<Plugin, T> mapping() {
		return mapping;
	}

	public PluginResourceListener<T> bindTo(PluginManager pluginManager) {
		if (listener != null) {
			throw new IllegalStateException("Already binded");
		}

		listener = CollectionUtils.addDiffListener(pluginManager.enabledPluginsList(),
				added -> {
					URL url = added.getBundle().getResource(resource);
					if (url != null) {
						LOGGER.fine("Found " + url + " in " + added.getDescription().getArtifact());
						T mapped;
						try (InputStream in = url.openStream()) {
							mapped = Objects.requireNonNull(mapper.apply(in), "mapper function returned null");
						} catch (Exception e) {
							LOGGER.log(Level.WARNING, "Couldn't read " + url, e);
							return;
						}
						mapping.put(added, mapped);
						invoke(addActions, added, mapped);
					}
				},
				removed -> {
					T mapped = mapping.remove(removed);
					if (mapped != null) {
						invoke(removeActions, removed, mapped);
					}
				});
		return this;
	}

	public void unbind() {
		if (listener != null) {
			listener = null;
		}
	}

	private void invoke(List<BiConsumer<Plugin, T>> listeners, Plugin plugin, T mapped) {
		for (BiConsumer<Plugin, T> listener : listeners) {
			try {
				listener.accept(plugin, mapped);
			} catch (Throwable e) {
				LOGGER.log(Level.WARNING, "Uncaught exception from service tracker listener " + listener, e);
			}
		}
	}

}
