package org.to2mbn.lolixl.ui.container.presenter;

import javafx.fxml.FXMLLoader;
import org.osgi.framework.BundleContext;
import org.to2mbn.lolixl.ui.container.view.View;
import org.to2mbn.lolixl.utils.BundleUtils;

import java.io.IOException;

public abstract class Presenter<T extends View> {
	protected T view;

	public Presenter(BundleContext ctx) {}

	public void initializeView() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setClassLoader(getClass().getClassLoader());
		loader.load(BundleUtils.getInputStreamFromBundle(getClass(), getFxmlLocation()));
		view = loader.getController();
		// FXMLLoader 会自动close掉InputStream
	}

	public void postInitialize() {}

	public T getView() {
		return view;
	}

	protected void registerService(BundleContext ctx) {}

	protected abstract String getFxmlLocation();
}