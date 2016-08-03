package org.to2mbn.lolixl.ui.container.presenter;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.to2mbn.lolixl.ui.container.view.View;
import org.to2mbn.lolixl.utils.BundleUtils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Presenter<T extends View> {

	private static final Logger LOGGER = Logger.getLogger(Presenter.class.getCanonicalName());

	protected T view;

	protected void active() {
		Platform.runLater(() -> {
			try {
				initializeView();
				initializePresenter();
				LOGGER.fine("Initialized presenter [" + getClass().getName() + "]");
			} catch (Throwable e) {
				LOGGER.log(Level.SEVERE, "Couldn't initialize presenter [" + getClass().getName() + "]", e);
			}
		});
	}

	private void initializeView() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setClassLoader(getClass().getClassLoader());
		loader.load(BundleUtils.getInputStreamFromBundle(getClass(), getFxmlLocation()));
		view = loader.getController();
		// FXMLLoader 会自动close掉InputStream
	}

	protected void initializePresenter() {}

	public T getView() {
		return view;
	}

	protected abstract String getFxmlLocation();
}