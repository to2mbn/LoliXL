package org.to2mbn.lolixl.utils;

import java.lang.ref.WeakReference;
import java.util.Objects;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

public class ObserverAdapter implements InvalidationListener {

	private WeakReference<ObservableContext> adapted;

	public ObserverAdapter(ObservableContext adapted) {
		this.adapted = new WeakReference<ObservableContext>(Objects.requireNonNull(adapted));
	}

	@Override
	public void invalidated(Observable observable) {
		ObservableContext ctx = adapted.get();
		if (ctx == null) {
			observable.removeListener(this);
		} else {
			ctx.notifyChanged();
		}
	}

}
