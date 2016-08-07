package org.to2mbn.lolixl.ui;

import javafx.scene.image.ImageView;

public final class GameVersionTagsManager {
	public enum GameVersionTagTypes {
		PURE_MC("game-version-tag-pure"),
		FORGE_RELEASE("game-version-tag-forge-release"),
		FORGE_BETA("game-version-tag-forge-beta"),
		FORGE_DEV("game-version-tag-forge-dev"),
		LITELOADER_RELEASE("game-version-tag-liteloader-release"),
		LITELOADER_BETA("game-version-tag-liteloader-beta"),
		LITELOADER_DEV("game-version-tag-liteloader-dev");

		private final String styleSheetsId;

		GameVersionTagTypes(String _styleSheetsId) {
			styleSheetsId = _styleSheetsId;
		}
	}

	private GameVersionTagsManager() {}

	public static void initStyle(GameVersionTagTypes type, ImageView view) {
		view.setId(type.styleSheetsId);
	}
}
