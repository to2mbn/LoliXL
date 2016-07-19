package org.to2mbn.lolixl.utils;

import javafx.application.Platform;

public final class FXUtils {

	private FXUtils() {}

	public static void checkFxThread() {
		if (!Platform.isFxApplicationThread())
			throw new IllegalStateException("Not on FX application thread; currentThread = " + Thread.currentThread().getName());
	}

}
