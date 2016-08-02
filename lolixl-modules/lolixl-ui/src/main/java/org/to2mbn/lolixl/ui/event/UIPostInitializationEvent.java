package org.to2mbn.lolixl.ui.event;

import org.osgi.service.event.Event;
import java.util.Collections;

public class UIPostInitializationEvent extends Event {
	public static final String TOPIC_POST_INITIALIZE = "org/to2mbn/lolixl/ui/postInitialize";

	public UIPostInitializationEvent() {
		super(TOPIC_POST_INITIALIZE, Collections.emptyMap());
	}
}
