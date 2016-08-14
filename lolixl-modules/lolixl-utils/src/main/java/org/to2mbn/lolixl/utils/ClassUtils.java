package org.to2mbn.lolixl.utils;

import java.util.function.Supplier;

public final class ClassUtils {

	private static class DummySecurityManager extends SecurityManager {

		@Override
		public Class<?>[] getClassContext() {
			return super.getClassContext();
		}
	}

	private static final DummySecurityManager dummy_security_manager = new DummySecurityManager();

	private ClassUtils() {}

	public static <T> T doWithContextClassLoader(ClassLoader ctxLoader, Supplier<T> action) {
		ClassLoader origin = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(ctxLoader);
		try {
			return action.get();
		} finally {
			Thread.currentThread().setContextClassLoader(origin);
		}
	}

	/**
	 * index 2 is the caller of this method.
	 * 
	 * @return
	 */
	public static Class<?>[] getClassContext() {
		return dummy_security_manager.getClassContext();
	}

}
