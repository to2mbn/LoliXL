package org.to2mbn.lolixl.ui.impl.auth;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.to2mbn.lolixl.core.config.Configuration;

public class AuthenticationProfileList implements Configuration {

	private static final long serialVersionUID = 1L;

	public List<AuthenticationProfileEntry> entries = new CopyOnWriteArrayList<>();

}
