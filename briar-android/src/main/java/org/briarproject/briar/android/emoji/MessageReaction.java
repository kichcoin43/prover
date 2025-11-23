package org.briarproject.briar.android.emoji;

import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class MessageReaction {

	private final String messageId;
	private final String authorId;
	private final String emoji;
	private final long timestamp;

	public MessageReaction(String messageId, String authorId, String emoji, long timestamp) {
		this.messageId = messageId;
		this.authorId = authorId;
		this.emoji = emoji;
		this.timestamp = timestamp;
	}

	public String getMessageId() {
		return messageId;
	}

	public String getAuthorId() {
		return authorId;
	}

	public String getEmoji() {
		return emoji;
	}

	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MessageReaction that = (MessageReaction) o;
		return messageId.equals(that.messageId) && authorId.equals(that.authorId);
	}

	@Override
	public int hashCode() {
		int result = messageId.hashCode();
		result = 31 * result + authorId.hashCode();
		return result;
	}
}
