package org.to2mbn.lolixl.ui.container.view;

import javafx.scene.Node;

public abstract class View {
	public <T extends Node> T getComponent(Class<T> type, String fxId) {
		Object component = getComponentImpl(fxId);
		if (component == null || type.isAssignableFrom(component.getClass())) {
			return null;
		}
		return (T) component;
	}

	private Object getComponentImpl(String name) {
		try {
			return getClass().getDeclaredField(name).get(this);
		} catch (Throwable error) {
			// TODO log warning?
			return null;
		}
	}
}
