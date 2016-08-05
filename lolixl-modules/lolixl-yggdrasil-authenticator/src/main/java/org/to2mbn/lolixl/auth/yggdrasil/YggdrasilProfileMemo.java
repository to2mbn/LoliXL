package org.to2mbn.lolixl.auth.yggdrasil;

import java.io.Serializable;
import org.to2mbn.jmccc.auth.yggdrasil.core.Session;

class YggdrasilProfileMemo implements Serializable {

	private static final long serialVersionUID = 1L;

	String email;
	Session session;

	public YggdrasilProfileMemo(String email, Session session) {
		this.email = email;
		this.session = session;
	}

}
