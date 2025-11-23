package org.briarproject.briar.android.emoji;

import android.content.Context;

import org.briarproject.bramble.api.db.DatabaseExecutor;
import org.briarproject.bramble.api.db.DbException;
import org.briarproject.bramble.api.lifecycle.LifecycleManager.OpenDatabaseHook;
import org.briarproject.bramble.api.settings.Settings;
import org.briarproject.bramble.api.settings.SettingsManager;
import org.briarproject.bramble.api.system.AndroidExecutor;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import static java.util.logging.Level.WARNING;
import static org.briarproject.bramble.util.LogUtils.logException;
import static org.briarproject.briar.android.settings.SettingsFragment.SETTINGS_NAMESPACE;

@Singleton
@NotNullByDefault
public class AnimatedEmojiManager implements OpenDatabaseHook {

        private static final Logger LOG =
                        Logger.getLogger(AnimatedEmojiManager.class.getName());

        private static final String USER_EMOJI_PREFERENCE = "pref_user_animated_emoji";
        private static final String OWN_EMOJI_PREFERENCE = "pref_own_animated_emoji";

        private final Context context;
        private final Executor dbExecutor;
        private final AndroidExecutor androidExecutor;
        private final SettingsManager settingsManager;
        private final Map<String, AnimatedEmoji> availableEmojis = new HashMap<>();
        private final Map<String, String> userEmojis = new HashMap<>();
        private String ownEmojiId = null;

        @Inject
        public AnimatedEmojiManager(Context context,
                        @DatabaseExecutor Executor dbExecutor,
                        AndroidExecutor androidExecutor,
                        SettingsManager settingsManager) {
                this.context = context;
                this.dbExecutor = dbExecutor;
                this.androidExecutor = androidExecutor;
                this.settingsManager = settingsManager;
                loadAvailableEmojis();
        }

        private void loadAvailableEmojis() {
                // Load only the 3 premium watch emojis
                availableEmojis.put("watch1", new AnimatedEmoji("watch1", "Watch 1", "emojis/watch1.tgs", "premium"));
                availableEmojis.put("watch2", new AnimatedEmoji("watch2", "Watch 2", "emojis/watch2.tgs", "premium"));
                availableEmojis.put("watch3", new AnimatedEmoji("watch3", "Watch 3", "emojis/watch3.tgs", "premium"));
        }

        public List<AnimatedEmoji> getEmojisByCategory(String category) {
                List<AnimatedEmoji> result = new ArrayList<>();
                for (AnimatedEmoji emoji : availableEmojis.values()) {
                        if (emoji.getCategory().equals(category)) {
                                result.add(emoji);
                        }
                }
                return result;
        }

        public List<AnimatedEmoji> getAllEmojis() {
                return new ArrayList<>(availableEmojis.values());
        }

        public List<String> getCategories() {
                List<String> categories = new ArrayList<>();
                for (AnimatedEmoji emoji : availableEmojis.values()) {
                        if (!categories.contains(emoji.getCategory())) {
                                categories.add(emoji.getCategory());
                        }
                }
                return categories;
        }

        @Nullable
        public AnimatedEmoji getEmojiById(String id) {
                return availableEmojis.get(id);
        }

        public void setUserEmoji(String contactId, String emojiId) {
                userEmojis.put(contactId, emojiId);
                saveUserEmojis();
        }

        @Nullable
        public String getUserEmojiId(String contactId) {
                return userEmojis.get(contactId);
        }

        @Nullable
        public AnimatedEmoji getUserEmoji(String contactId) {
                String emojiId = userEmojis.get(contactId);
                if (emojiId == null) return null;
                return availableEmojis.get(emojiId);
        }

        public void setOwnEmoji(@Nullable String emojiId) {
                this.ownEmojiId = emojiId;
                saveOwnEmoji();
        }

        @Nullable
        public String getOwnEmojiId() {
                return ownEmojiId;
        }

        @Nullable
        public AnimatedEmoji getOwnEmoji() {
                if (ownEmojiId == null) return null;
                return availableEmojis.get(ownEmojiId);
        }

        private void saveOwnEmoji() {
                dbExecutor.execute(() -> {
                        Settings settings = new Settings();
                        if (ownEmojiId != null) {
                                settings.put(OWN_EMOJI_PREFERENCE, ownEmojiId);
                        } else {
                                settings.put(OWN_EMOJI_PREFERENCE, "");
                        }
                        try {
                                settingsManager.mergeSettings(settings, SETTINGS_NAMESPACE);
                        } catch (DbException e) {
                                logException(LOG, WARNING, e);
                        }
                });
        }

        private void saveUserEmojis() {
                dbExecutor.execute(() -> {
                        Settings settings = new Settings();
                        StringBuilder sb = new StringBuilder();
                        for (Map.Entry<String, String> entry : userEmojis.entrySet()) {
                                if (sb.length() > 0) sb.append(";");
                                sb.append(entry.getKey()).append(":").append(entry.getValue());
                        }
                        settings.put(USER_EMOJI_PREFERENCE, sb.toString());
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
                String serialized = settings.get(USER_EMOJI_PREFERENCE);
                if (serialized != null && !serialized.isEmpty()) {
                        androidExecutor.runOnUiThread(() -> {
                                String[] pairs = serialized.split(";");
                                for (String pair : pairs) {
                                        String[] parts = pair.split(":");
                                        if (parts.length == 2) {
                                                userEmojis.put(parts[0], parts[1]);
                                        }
                                }
                        });
                }
                
                String ownEmoji = settings.get(OWN_EMOJI_PREFERENCE);
                if (ownEmoji != null && !ownEmoji.isEmpty()) {
                        androidExecutor.runOnUiThread(() -> {
                                ownEmojiId = ownEmoji;
                        });
                }
        }
}
