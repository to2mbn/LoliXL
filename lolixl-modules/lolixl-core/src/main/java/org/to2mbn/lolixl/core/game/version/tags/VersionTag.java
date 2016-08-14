package org.to2mbn.lolixl.core.game.version.tags;

import java.util.List;
import java.util.Objects;
import org.to2mbn.lolixl.i18n.I18N;
import javafx.beans.value.ObservableStringValue;

public class VersionTag implements Comparable<VersionTag> {

	private String id;
	private int ranking;
	private List<String> cssClasses;
	private ObservableStringValue displayName;

	public VersionTag(String id, int ranking, List<String> cssClasses) {
		this(id, ranking, cssClasses, I18N.localize(id));
	}

	public VersionTag(String id, int ranking, List<String> cssClasses, ObservableStringValue displayName) {
		this.id = Objects.requireNonNull(id);
		this.ranking = ranking;
		this.cssClasses = Objects.requireNonNull(cssClasses);
		this.displayName = Objects.requireNonNull(displayName);
	}

	public String getId() {
		return id;
	}

	public ObservableStringValue getDisplayName() {
		return displayName;
	}

	public int getRanking() {
		return ranking;
	}

	public List<String> getCssClasses() {
		return cssClasses;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof VersionTag) {
			VersionTag another = (VersionTag) obj;
			return id.equals(another.id);
		}
		return false;
	}

	@Override
	public int compareTo(VersionTag o) {
		if (ranking == o.ranking) {
			return id.compareTo(o.id);
		}
		return Integer.compare(ranking, o.ranking);
	}

}
