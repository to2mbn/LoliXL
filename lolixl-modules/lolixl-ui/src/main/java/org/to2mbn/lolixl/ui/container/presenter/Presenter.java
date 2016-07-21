package org.to2mbn.lolixl.ui.container.presenter;

import javafx.fxml.FXMLLoader;
import org.to2mbn.lolixl.ui.container.view.View;
import org.to2mbn.lolixl.utils.BundleUtils;

import java.io.IOException;

public abstract class Presenter<T extends View> {
	protected T view;

	protected void initialize(String fxmlLocation) throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setClassLoader(getClass().getClassLoader());
		loader.load(BundleUtils.getInputStreamFromBundle(getClass(), fxmlLocation));
		view = loader.getController();
		// FXMLLoader 会自动close掉InputStream
	}

	public T getView() {
		return view;
	}
}