package org.to2mbn.lolixl.utils;

import java.util.Objects;
import java.util.function.DoubleFunction;
import javafx.animation.Interpolator;

public class FunctionInterpolator extends Interpolator {

	public static final Interpolator S_CURVE = new FunctionInterpolator(t -> t <= 0.5 ? 4 * t * t * t : 4 * (t - 1) * (t - 1) * (t - 1) + 1);

	private DoubleFunction<Double> func;

	public FunctionInterpolator(DoubleFunction<Double> func) {
		this.func = Objects.requireNonNull(func);
	}

	@Override
	protected double curve(double t) {
		return func.apply(t);
	}

}
