package org.to2mbn.lolixl.core.game.auth;

import org.to2mbn.lolixl.ui.DisplayableTile;

/**
 * OSGi的一个服务，代表了一种验证方法。
 * <p>
 * 由AuthenticationService可以创建出AuthenticationProfile，
 * AuthenticationProfile代表了一个用户可以选择的账号（出现在账号列表里）， 可以在运行时提供验证信息。
 * AuthenticationProfile的存储使用了备忘录模式。
 * 
 * @author yushijinhun
 */
public interface AuthenticationService extends DisplayableTile {

	String PROPERTY_AUTH_METHOD = "org.to2mbn.lolixl.core.game.auth.method";

	AuthenticationProfile<?> createProfile();

}
