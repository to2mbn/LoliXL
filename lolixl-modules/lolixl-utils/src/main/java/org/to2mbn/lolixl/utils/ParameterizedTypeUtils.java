package org.to2mbn.lolixl.utils;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Objects;

public final class ParameterizedTypeUtils {

	private ParameterizedTypeUtils() {}

	private static class ParameterizedTypeImpl implements ParameterizedType {

		private Type[] actualTypeArguments;
		private Class<?> rawType;
		private Type ownerType;

		private ParameterizedTypeImpl(Class<?> rawType, Type[] actualTypeArguments) {
			this.actualTypeArguments = actualTypeArguments;
			this.rawType = rawType;
			ownerType = rawType.getDeclaringClass();

			TypeVariable<?>[] formals = rawType.getTypeParameters();
			if (formals.length != actualTypeArguments.length) {
				throw new MalformedParameterizedTypeException();
			}
		}

		@Override
		public Type[] getActualTypeArguments() {
			return actualTypeArguments.clone();
		}

		@Override
		public Class<?> getRawType() {
			return rawType;
		}

		@Override
		public Type getOwnerType() {
			return ownerType;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj instanceof ParameterizedType) {
				ParameterizedType another = (ParameterizedType) obj;
				return Objects.equals(ownerType, another.getOwnerType())
						&& Objects.equals(rawType, another.getRawType())
						&& Arrays.equals(actualTypeArguments, another.getActualTypeArguments());
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(actualTypeArguments) ^
					Objects.hashCode(ownerType) ^
					Objects.hashCode(rawType);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			if (ownerType != null) {
				if (ownerType instanceof Class) {
					sb.append(((Class<?>) ownerType).getName());
				} else {
					sb.append(ownerType.toString());
				}
				sb.append(".");
				if (ownerType instanceof ParameterizedTypeImpl) {
					sb.append(rawType.getName().replace(((ParameterizedTypeImpl) ownerType).rawType.getName() + "$", ""));
				} else {
					sb.append(rawType.getName());
				}
			} else {
				sb.append(rawType.getName());
			}
			if (actualTypeArguments != null && actualTypeArguments.length > 0) {
				sb.append("<");
				boolean first = true;
				for (Type t : actualTypeArguments) {
					if (!first) {
						sb.append(", ");
					}
					sb.append(t.getTypeName());
					first = false;
				}
				sb.append(">");
			}
			return sb.toString();
		}
	}

	public static ParameterizedType createParameterizedType(Class<?> rawType, Type... actualTypeArguments) {
		return new ParameterizedTypeImpl(rawType, actualTypeArguments);
	}

}
