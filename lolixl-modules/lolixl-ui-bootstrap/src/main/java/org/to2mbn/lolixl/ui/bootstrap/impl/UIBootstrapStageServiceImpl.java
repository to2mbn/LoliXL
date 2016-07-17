package org.to2mbn.lolixl.ui.bootstrap.impl;

import javafx.stage.Stage;
import org.to2mbn.lolixl.ui.bootstrap.UIBootstrapStageService;

public class UIBootstrapStageServiceImpl implements UIBootstrapStageService {
	private final Stage stage;

	public UIBootstrapStageServiceImpl(Stage _stage) {
		stage = _stage;
	}

	@Override
	public Stage getPrimaryStage() {
		return stage;
	}
}
