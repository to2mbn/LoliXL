package org.to2mbn.lolixl.ui.container.presenter;

import javafx.fxml.FXMLLoader;
import org.to2mbn.lolixl.ui.container.view.View;

import java.io.IOException;
import java.net.URL;

public abstract class Presenter<T extends View> {
	protected T view;

	public void initialize(URL fxmlLocation) throws IOException {
		view = new FXMLLoader(fxmlLocation).getController();
	}

	public T getView() {
		return view;
	}
}