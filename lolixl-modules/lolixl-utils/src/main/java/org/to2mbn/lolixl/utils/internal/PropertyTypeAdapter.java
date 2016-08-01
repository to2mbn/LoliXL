package org.to2mbn.lolixl.utils.internal;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.to2mbn.lolixl.utils.ParameterizedTypeUtils;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.adapter.JavaBeanProperty;
import javafx.collections.FXCollections;

public class PropertyTypeAdapter implements JsonSerializer<Property<?>>, JsonDeserializer<Property<?>> {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Property<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		if (typeOfT instanceof Class) {
			Class<?> clazzType = (Class<?>) typeOfT;
			if (!JavaBeanProperty.class.isAssignableFrom(clazzType)) {
				if (BooleanProperty.class.isAssignableFrom(clazzType)) {
					return new SimpleBooleanProperty(context.deserialize(json, Boolean.class));
				} else if (IntegerProperty.class.isAssignableFrom(clazzType)) {
					return new SimpleIntegerProperty(context.deserialize(json, Integer.class));
				} else if (LongProperty.class.isAssignableFrom(clazzType)) {
					return new SimpleLongProperty(context.deserialize(json, Long.class));
				} else if (FloatProperty.class.isAssignableFrom(clazzType)) {
					return new SimpleFloatProperty(context.deserialize(json, Float.class));
				} else if (DoubleProperty.class.isAssignableFrom(clazzType)) {
					return new SimpleDoubleProperty(context.deserialize(json, Double.class));
				} else if (StringProperty.class.isAssignableFrom(clazzType)) {
					return new SimpleStringProperty(context.deserialize(json, String.class));
				}
			}
		} else if (typeOfT instanceof ParameterizedType) {
			Type rawType = ((ParameterizedType) typeOfT).getRawType();
			Type[] params = ((ParameterizedType) typeOfT).getActualTypeArguments();
			if (rawType instanceof Class) {
				Class<?> rawClass = (Class<?>) rawType;
				if (ListProperty.class.isAssignableFrom(rawClass) && params.length == 1) {
					List<?> result = (List<?>) context.deserialize(json, ParameterizedTypeUtils.createParameterizedType(List.class, params));
					return new SimpleListProperty(FXCollections.observableList(result));
				} else if (SetProperty.class.isAssignableFrom(rawClass) && params.length == 1) {
					Set<?> result = (Set<?>) context.deserialize(json, ParameterizedTypeUtils.createParameterizedType(Set.class, params));
					return new SimpleSetProperty(FXCollections.observableSet(result));
				} else if (MapProperty.class.isAssignableFrom(rawClass) && params.length == 2) {
					Map<?, ?> result = (Map<?, ?>) context.deserialize(json, ParameterizedTypeUtils.createParameterizedType(Map.class, params));
					return new SimpleMapProperty(FXCollections.observableMap(result));
				} else if (ObjectProperty.class.isAssignableFrom(rawClass) && params.length == 1) {
					return new SimpleObjectProperty(context.deserialize(json, params[0]));
				}
			}
		}
		return null;
	}

	@Override
	public JsonElement serialize(Property<?> src, Type typeOfSrc, JsonSerializationContext context) {
		if (!(src instanceof JavaBeanProperty)) {
			return context.serialize(src.getValue());
		}
		return null;
	}

}
