package org.to2mbn.lolixl.utils;

public final class MathUtils {

	private MathUtils() {}

	public static double clamp(double min, double value, double max) {
		if (value < min) {
			return min;
		} else if (value > max) {
			return max;
		} else {
			return value;
		}
	}

}
