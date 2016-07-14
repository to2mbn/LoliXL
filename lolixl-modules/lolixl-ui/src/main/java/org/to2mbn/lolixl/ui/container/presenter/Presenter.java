package org.to2mbn.lolixl.ui.container.presenter;

import javafx.fxml.FXMLLoader;
import org.to2mbn.lolixl.ui.container.view.View;
import org.to2mbn.lolixl.utils.LazyReference;

import java.io.IOException;
import java.net.URL;

public abstract class Presenter {
	public final LazyReference<View> view = new LazyReference<View>();

	public void initialize(URL fxmlLocation) throws IOException {
		FXMLLoader loader = new FXMLLoader(fxmlLocation);
		view.set(loader.getController());
	}
}
