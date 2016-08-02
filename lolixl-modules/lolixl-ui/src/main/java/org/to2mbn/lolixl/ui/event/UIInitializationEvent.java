package org.to2mbn.lolixl.ui.event;

import org.osgi.service.event.Event;
import java.util.Collections;

public class UIInitializationEvent extends Event {
	public static final String TOPIC_INITIALIZE = "org/to2mbn/lolixl/ui/initialize";

	public UIInitializationEvent() {
		super(TOPIC_INITIALIZE, Collections.emptyMap());
	}
}
