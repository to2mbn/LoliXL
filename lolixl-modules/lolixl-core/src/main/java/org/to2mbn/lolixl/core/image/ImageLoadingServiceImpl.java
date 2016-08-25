package org.to2mbn.lolixl.core.image;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.lolixl.ui.ImageLoadingService;
import org.to2mbn.lolixl.utils.ClassUtils;
import org.to2mbn.lolixl.utils.binding.FxConstants;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.image.Image;

@Service({ ImageLoadingService.class })
@Component(immediate = true)
public class ImageLoadingServiceImpl implements ImageLoadingService {

	@Override
	public ObservableObjectValue<Image> load(String location, ClassLoader caller) {
		// TODO: 使图像可以通过Theme修改
		// TODO: 图像加载缓存
		return FxConstants.object(ClassUtils.doWithContextClassLoader(caller, () -> new Image(location)));
	}

}
