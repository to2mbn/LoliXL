package org.to2mbn.lolixl.ui.model;

import java.util.Objects;
import java.util.Optional;
import javafx.scene.image.Image;

public class PanelHeader {

	private String title;
	private Optional<Image> icon;

	public PanelHeader(String title, Optional<Image> icon) {
		this.title = Objects.requireNonNull(title);
		this.icon = Objects.requireNonNull(icon);
	}

	public String getTitle() {
		return title;
	}

	public Optional<Image> getIcon() {
		return icon;
	}
}
