package org.to2mbn.lolixl.utils;

import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ObservableDoubleValue;

public final class GlobalVariables {

	public static final String PROPERTY_GLOBAL_VARIABLE = "org.to2mbn.lolixl.globalVariable";

	/**
	 * Type: {@link ObservableDoubleValue}, {@link DoubleProperty}
	 */
	public static final String VALUE_ANIMATION_TIME_MULTIPLIER = "org.to2mbn.lolixl.ui.animation.timeMultiplier";

	public static final String ANIMATION_TIME_MULTIPLIER = "(" + PROPERTY_GLOBAL_VARIABLE + "=" + VALUE_ANIMATION_TIME_MULTIPLIER + ")";

	private GlobalVariables() {}

}
