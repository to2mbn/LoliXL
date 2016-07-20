package org.to2mbn.lolixl.ui.container.presenter;

import javafx.fxml.FXMLLoader;
import org.to2mbn.lolixl.ui.container.view.View;

import java.io.IOException;
import java.io.InputStream;

public abstract class Presenter<T extends View> {
	protected T view;

	public void initialize(InputStream fxml) throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setClassLoader(getClass().getClassLoader());
		loader.load(fxml);
		view = loader.getController();
		// FXMLLoader 会自动close掉InputStream
	}

	public T getView() {
		return view;
	}
}