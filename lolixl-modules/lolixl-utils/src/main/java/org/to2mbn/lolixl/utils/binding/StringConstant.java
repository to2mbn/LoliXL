package org.to2mbn.lolixl.utils.binding;

import javafx.beans.value.ObservableStringValue;

class StringConstant extends ObjectConstant<String> implements ObservableStringValue {

	public StringConstant(String value) {
		super(value);
	}

}
