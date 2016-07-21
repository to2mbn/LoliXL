package org.to2mbn.lolixl.core.impl.auth;

import java.io.Serializable;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuthenticationProfileList implements Serializable {

	private static final long serialVersionUID = 1L;

	public CopyOnWriteArrayList<AuthenticationProfileEntry> entries;

}
