package org.to2mbn.lolixl.ui;

import org.to2mbn.lolixl.utils.ClassUtils;
import org.to2mbn.lolixl.utils.ServiceUtils;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.image.Image;

public final class ImageLoading {

	private ImageLoading() {}

	public static ObservableObjectValue<Image> load(String location) {
		Class<?> caller = ClassUtils.getClassContext()[3];
		return ServiceUtils.doWithService(ImageLoadingService.class, caller, service -> service.load(location, caller.getClassLoader()));

	}

}
