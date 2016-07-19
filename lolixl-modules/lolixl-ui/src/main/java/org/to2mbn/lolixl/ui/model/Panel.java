package org.to2mbn.lolixl.ui.model;

import java.util.Objects;
import java.util.Optional;
import javafx.scene.layout.Pane;

public class Panel {

	private Optional<PanelHeader> header;
	private Pane content;

	public Panel(Optional<PanelHeader> header, Pane content) {
		this.header = Objects.requireNonNull(header);
		this.content = Objects.requireNonNull(content);
	}

	public Optional<PanelHeader> getHeader() {
		return header;
	}

	public Pane getContent() {
		return content;
	}

}
