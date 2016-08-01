package org.to2mbn.lolixl.ui.impl.event;

import org.osgi.service.event.Event;
import org.to2mbn.lolixl.ui.impl.UIApp;
import java.util.Dictionary;
import java.util.Hashtable;

public class UIPostInitializationEvent extends Event {
	public static final String TOPIC_DOWNLOAD_START = "org/to2mbn/lolixl/ui-impl/postInitialize";
	public static final String KEY_UI_INSTANCE = "org.to2mbn.lolixl.ui-impl.postInitialize.uiInstance";

	public UIPostInitializationEvent(UIApp uiApp) {
		super(TOPIC_DOWNLOAD_START, makeProperties(uiApp));
	}

	private static Dictionary<String, Object> makeProperties(UIApp uiApp) {
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put(KEY_UI_INSTANCE, uiApp);
		return properties;
	}
}
