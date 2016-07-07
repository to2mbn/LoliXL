package org.to2mbn.lolixl.plugin.maven;

public class IllegalVersionException extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;

	public IllegalVersionException() {}

	public IllegalVersionException(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalVersionException(String s) {
		super(s);
	}

	public IllegalVersionException(Throwable cause) {
		super(cause);
	}

}
