package org.to2mbn.lolixl.i18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableValue;

public class LocalizedStringValue extends StringBinding {

	private static Object extractValue(Object arg) {
		return arg instanceof ObservableValue ? ((ObservableValue<?>) arg).getValue() : arg;
	}

	private static Object[] extractValues(Object[] args) {
		int n = args.length;
		Object[] values = new Object[n];
		for (int i = 0; i < n; i++) {
			values[i] = extractValue(args[i]);
		}
		return values;
	}

	private static ObservableValue<?>[] extractDependencies(Object[] args) {
		List<ObservableValue<?>> dependencies = new ArrayList<ObservableValue<?>>();
		for (Object arg : args) {
			if (arg instanceof ObservableValue) {
				dependencies.add((ObservableValue<?>) arg);
			}
		}
		return dependencies.toArray(new ObservableValue[dependencies.size()]);
	}

	private String key;
	private Object[] args;

	public LocalizedStringValue(LocalizationService localizationService, String key, Object... args) {
		this.key = Objects.requireNonNull(key);
		this.args = Objects.requireNonNull(args);

		bind(localizationService.localeProperty());
		bind(extractDependencies(args));
	}

	@Override
	protected String computeValue() {
		return I18N.getLocalizedString(key, extractValues(args));
	}

}
