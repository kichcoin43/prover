package org.briarproject.briar.android.emoji;

import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class AnimatedEmoji {

	private final String id;
	private final String name;
	private final String assetPath;
	private final String category;

	public AnimatedEmoji(String id, String name, String assetPath, String category) {
		this.id = id;
		this.name = name;
		this.assetPath = assetPath;
		this.category = category;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getAssetPath() {
		return assetPath;
	}

	public String getCategory() {
		return category;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AnimatedEmoji that = (AnimatedEmoji) o;
		return id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
