package org.to2mbn.lolixl.core.auth;

import java.io.Serializable;

/**
 * 提供一种验证方法。
 * <p>
 * AuthenticationService是OSGi的服务，代表了一种验证方法。
 * 由AuthenticationService可以创建出AuthenticationProfile，
 * AuthenticationProfile代表了一个用户可以选择的账号（出现在账号列表里）， 可以在运行时提供验证信息。 调用
 * {@link AuthenticationProfile#getConfiguration()}
 * 可以获得该AuthenticationProfile的配置，配置会被持久化存储。
 * 而AuthenticationService又可以根据配置创建AuthenticationProfile。
 * <p>
 * （下图如果变形，请将编辑器调为等宽字体并直接看源码中的Javadoc）
 * <pre>
 * +---------------------+  createProfile()  +---------------------+
 * |AuthenticationService|------------------>|AuthenticationProfile|
 * +---------------------+    |              +---------------------+
 *                            |                  | getConfiguration()
 *       createProfile(CONFIG)|                 \|/
 *                            |              +------+
 *                            \--------------|CONFIG|
 *                                           +------+
 * </pre>
 * 
 * @param <CONFIG> 配置的类型
 * @author yushijinhun
 */
public interface AuthenticationService<CONFIG extends Serializable> {

	String PROPERTY_AUTH_METHOD = "org.to2mbn.lolixl.core.auth.method";

	String getLocalizedName();

	AuthenticationProfile<CONFIG> createProfile();

	AuthenticationProfile<CONFIG> createProfile(CONFIG configuration);

	Class<CONFIG> getConfigurationType();

}
