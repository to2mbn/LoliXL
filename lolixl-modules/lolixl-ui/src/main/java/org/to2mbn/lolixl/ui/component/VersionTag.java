package org.to2mbn.lolixl.ui.component;

public class VersionTag {
	public static final VersionTag PURE_RELEASE = new VersionTag("game-version-tag-pure-release");
	public static final VersionTag PURE_SNAPSHOT = new VersionTag("game-version-tag-pure-snapshot");
	public static final VersionTag PURE_BETA = new VersionTag("game-version-tag-pure-beta");
	public static final VersionTag PURE_OLD = new VersionTag("game-version-tag-pure-old");
	public static final VersionTag FORGE_RELEASE = new VersionTag("game-version-tag-forge-release");
	public static final VersionTag FORGE_BETA = new VersionTag("game-version-tag-forge-beta");
	public static final VersionTag FORGE_DEV = new VersionTag("game-version-tag-forge-dev");
	public static final VersionTag LITELOADER_RELEASE = new VersionTag("game-version-tag-liteloader-release");
	public static final VersionTag LITELOADER_DEV = new VersionTag("game-version-tag-liteloader-dev");

	private final String cssId;

	public VersionTag(String _cssId) {
		cssId = _cssId;
	}

	public String getCssId() {
		return cssId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		VersionTag that = (VersionTag) o;
		return cssId != null ? cssId.equals(that.cssId) : that.cssId == null;

	}

	@Override
	public int hashCode() {
		return cssId != null ? cssId.hashCode() : 0;
	}
}
