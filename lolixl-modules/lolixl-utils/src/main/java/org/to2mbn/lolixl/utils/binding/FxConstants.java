package org.to2mbn.lolixl.utils.binding;

import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableStringValue;

public final class FxConstants {

	private FxConstants() {}

	public static <T> ObservableObjectValue<T> object(T constant) {
		return new ObjectConstant<T>(constant);
	}

	public static ObservableStringValue string(String constant) {
		return new StringConstant(constant);
	}

}
