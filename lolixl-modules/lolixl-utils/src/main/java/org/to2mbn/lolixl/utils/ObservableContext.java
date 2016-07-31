package org.to2mbn.lolixl.utils;

import java.util.List;
import java.util.Vector;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

public class ObservableContext implements Observable {

	private ObserverAdapter observer = new ObserverAdapter(this);
	private List<InvalidationListener> listeners = new Vector<>();

	public void notifyChanged() {
		InvalidationListener[] listenersArray = listeners.toArray(new InvalidationListener[listeners.size()]);
		for (InvalidationListener listener : listenersArray) {
			try {
				listener.invalidated(this);
			} catch (Throwable e) {
				Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
			}
		}
	}

	public void bind(Observable... dependencies) {
		if ((dependencies != null) && (dependencies.length > 0)) {
			for (Observable dep : dependencies) {
				dep.addListener(observer);
			}
		}
	}

	public void unbind(Observable... dependencies) {
		if (observer != null) {
			for (Observable dep : dependencies) {
				dep.removeListener(observer);
			}
		}
	}

	@Override
	public void addListener(InvalidationListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(InvalidationListener listener) {
		listeners.remove(listener);
	}

}
