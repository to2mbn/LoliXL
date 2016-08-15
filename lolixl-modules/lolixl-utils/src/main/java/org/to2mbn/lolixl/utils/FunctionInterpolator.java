package org.to2mbn.lolixl.utils;

import java.util.Objects;
import java.util.function.DoubleFunction;
import javafx.animation.Interpolator;

public class FunctionInterpolator extends Interpolator {

	private DoubleFunction<Double> func;

	public FunctionInterpolator(DoubleFunction<Double> func) {
		this.func = Objects.requireNonNull(func);
	}

	@Override
	protected double curve(double t) {
		return func.apply(t);
	}

}
