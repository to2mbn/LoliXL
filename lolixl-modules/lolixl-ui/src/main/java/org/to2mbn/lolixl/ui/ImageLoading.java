package org.to2mbn.lolixl.ui;

import org.to2mbn.lolixl.utils.ClassUtils;
import org.to2mbn.lolixl.utils.ServiceUtils;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.image.Image;

public final class ImageLoading {

	private ImageLoading() {}

	public static ObservableObjectValue<Image> load(String location) {
		return load0(location, 4);
	}

	private static ObservableObjectValue<Image> load0(String location, int frames) {
		Class<?> caller = ClassUtils.getClassContext()[frames];
		return ServiceUtils.doWithService(ImageLoadingService.class, caller, service -> service.load(location, caller.getClassLoader()));
	}

}
