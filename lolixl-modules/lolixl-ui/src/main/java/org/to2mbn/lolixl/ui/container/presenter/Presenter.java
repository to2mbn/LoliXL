package org.to2mbn.lolixl.ui.container.presenter;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.to2mbn.lolixl.ui.container.view.View;
import org.to2mbn.lolixl.utils.LazyReference;

import java.io.IOException;
import java.net.URL;

public abstract class Presenter<V extends View, C extends Parent> {
	public final LazyReference<V> view = new LazyReference<V>();
	public final LazyReference<C> content = new LazyReference<C>();

	public void initialize(URL fxmlLocation) throws IOException {
		FXMLLoader loader = new FXMLLoader(fxmlLocation);
		view.set(loader.getController());
		content.set(loader.load());
	}
}
