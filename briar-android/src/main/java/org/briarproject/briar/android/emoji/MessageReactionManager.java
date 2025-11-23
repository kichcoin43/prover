package org.briarproject.briar.android.emoji;

import org.briarproject.bramble.api.db.DatabaseExecutor;
import org.briarproject.bramble.api.db.DbException;
import org.briarproject.bramble.api.lifecycle.LifecycleManager.OpenDatabaseHook;
import org.briarproject.bramble.api.settings.Settings;
import org.briarproject.bramble.api.settings.SettingsManager;
import org.briarproject.bramble.api.system.AndroidExecutor;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import static java.util.logging.Level.WARNING;
import static org.briarproject.bramble.util.LogUtils.logException;
import static org.briarproject.briar.android.settings.SettingsFragment.SETTINGS_NAMESPACE;

@Singleton
@NotNullByDefault
public class MessageReactionManager implements OpenDatabaseHook {

	private static final Logger LOG =
			Logger.getLogger(MessageReactionManager.class.getName());

	private static final String REACTIONS_PREFERENCE = "pref_message_reactions";

	private final Executor dbExecutor;
	private final AndroidExecutor androidExecutor;
	private final SettingsManager settingsManager;
	private final Map<String, List<MessageReaction>> reactions = new HashMap<>();
	private final List<ReactionListener> listeners = new ArrayList<>();

	public interface ReactionListener {
		void onReactionAdded(MessageReaction reaction);
		void onReactionRemoved(MessageReaction reaction);
	}

	@Inject
	public MessageReactionManager(@DatabaseExecutor Executor dbExecutor,
			AndroidExecutor androidExecutor,
			SettingsManager settingsManager) {
		this.dbExecutor = dbExecutor;
		this.androidExecutor = androidExecutor;
		this.settingsManager = settingsManager;
	}

	public void addReaction(String messageId, String authorId, String emoji) {
		MessageReaction reaction = new MessageReaction(messageId, authorId, emoji,
				System.currentTimeMillis());
		
		List<MessageReaction> messageReactions = reactions.get(messageId);
		if (messageReactions == null) {
			messageReactions = new ArrayList<>();
			reactions.put(messageId, messageReactions);
		}

		for (int i = 0; i < messageReactions.size(); i++) {
			if (messageReactions.get(i).getAuthorId().equals(authorId)) {
				messageReactions.remove(i);
				break;
			}
		}

		messageReactions.add(reaction);
		saveReactions();
		notifyReactionAdded(reaction);
	}

	public void removeReaction(String messageId, String authorId) {
		List<MessageReaction> messageReactions = reactions.get(messageId);
		if (messageReactions != null) {
			for (int i = 0; i < messageReactions.size(); i++) {
				MessageReaction reaction = messageReactions.get(i);
				if (reaction.getAuthorId().equals(authorId)) {
					messageReactions.remove(i);
					saveReactions();
					notifyReactionRemoved(reaction);
					break;
				}
			}
		}
	}

	public List<MessageReaction> getReactions(String messageId) {
		List<MessageReaction> result = reactions.get(messageId);
		return result != null ? new ArrayList<>(result) : new ArrayList<>();
	}

	public int getReactionCount(String messageId) {
		List<MessageReaction> result = reactions.get(messageId);
		return result != null ? result.size() : 0;
	}

	public void addListener(ReactionListener listener) {
		listeners.add(listener);
	}

	public void removeListener(ReactionListener listener) {
		listeners.remove(listener);
	}

	private void notifyReactionAdded(MessageReaction reaction) {
		androidExecutor.runOnUiThread(() -> {
			for (ReactionListener listener : listeners) {
				listener.onReactionAdded(reaction);
			}
		});
	}

	private void notifyReactionRemoved(MessageReaction reaction) {
		androidExecutor.runOnUiThread(() -> {
			for (ReactionListener listener : listeners) {
				listener.onReactionRemoved(reaction);
			}
		});
	}

	private void saveReactions() {
		dbExecutor.execute(() -> {
			Settings settings = new Settings();
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, List<MessageReaction>> entry : reactions.entrySet()) {
				for (MessageReaction reaction : entry.getValue()) {
					if (sb.length() > 0) sb.append("|");
					sb.append(reaction.getMessageId()).append(":")
						.append(reaction.getAuthorId()).append(":")
						.append(reaction.getEmoji()).append(":")
						.append(reaction.getTimestamp());
				}
			}
			settings.put(REACTIONS_PREFERENCE, sb.toString());
			try {
				settingsManager.mergeSettings(settings, SETTINGS_NAMESPACE);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
	}

	@Override
	public void onDatabaseOpened(org.briarproject.bramble.api.db.Transaction txn)
			throws DbException {
		Settings settings = settingsManager.getSettings(txn, SETTINGS_NAMESPACE);
		String serialized = settings.get(REACTIONS_PREFERENCE);
		if (serialized != null && !serialized.isEmpty()) {
			androidExecutor.runOnUiThread(() -> {
				String[] items = serialized.split("\\|");
				for (String item : items) {
					String[] parts = item.split(":");
					if (parts.length == 4) {
						String messageId = parts[0];
						String authorId = parts[1];
						String emoji = parts[2];
						long timestamp = Long.parseLong(parts[3]);
						MessageReaction reaction = new MessageReaction(messageId, authorId,
								emoji, timestamp);
						List<MessageReaction> messageReactions = reactions.get(messageId);
						if (messageReactions == null) {
							messageReactions = new ArrayList<>();
							reactions.put(messageId, messageReactions);
						}
						messageReactions.add(reaction);
					}
				}
			});
		}
	}
}
