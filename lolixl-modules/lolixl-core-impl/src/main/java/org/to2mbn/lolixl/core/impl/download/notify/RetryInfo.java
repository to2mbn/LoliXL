package org.to2mbn.lolixl.core.impl.download.notify;

public class RetryInfo {

	int current;
	int max;
	Throwable ex;

	public int getCurrent() {
		return current;
	}

	public int getMax() {
		return max;
	}

	public Throwable getException() {
		return ex;
	}

}