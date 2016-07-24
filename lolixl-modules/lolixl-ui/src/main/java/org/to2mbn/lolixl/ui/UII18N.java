package org.to2mbn.lolixl.ui;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.to2mbn.lolixl.i18n.I18N;
import org.to2mbn.lolixl.i18n.LocaleChangedEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class UII18N implements EventHandler {
	private static class InstanceHolder {
		private static UII18N instance = new UII18N();
	}

	private final Map<String, List<Consumer<String>>> listeners = new ConcurrentHashMap<>();

	private UII18N() {
		BundleContext context = FrameworkUtil.getBundle(UII18N.class).getBundleContext();
		Dictionary<String, String> properties = new Hashtable<>();
		properties.put(EventConstants.EVENT_TOPIC, LocaleChangedEvent.TOPIC_LOCALE_CHANGED);
		context.registerService(EventHandler.class, InstanceHolder.instance, properties);
	}

	public UII18N instance() {
		return InstanceHolder.instance;
	}

	public void registerListener(String key, Consumer<String> listener) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(listener);
		listeners.computeIfAbsent(key, k -> Collections.synchronizedList(new LinkedList<>()));
		listeners.get(key).add(listener);
	}

	public void removeListenersByKey(String key) {
		Objects.requireNonNull(key);
		listeners.remove(key);
	}

	public List<Consumer<String>> getListeners(String key) {
		Objects.requireNonNull(key);
		return listeners.get(key);
	}

	@Override
	public void handleEvent(Event event) {
		Locale newLocale = (Locale) event.getProperty(LocaleChangedEvent.KEY_NEW_LOCALE);
		listeners.forEach((key, listenerList) -> listenerList.forEach(listener -> listener.accept(I18N.localize(newLocale, key))));
	}
}
