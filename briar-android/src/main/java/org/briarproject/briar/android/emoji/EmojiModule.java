package org.briarproject.briar.android.emoji;

import android.app.Application;

import org.briarproject.bramble.api.db.DatabaseExecutor;
import org.briarproject.bramble.api.lifecycle.LifecycleManager;
import org.briarproject.bramble.api.settings.SettingsManager;
import org.briarproject.bramble.api.system.AndroidExecutor;

import java.util.concurrent.Executor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class EmojiModule {

	@Provides
	@Singleton
	AnimatedEmojiManager provideAnimatedEmojiManager(
			Application app,
			@DatabaseExecutor Executor dbExecutor,
			AndroidExecutor androidExecutor,
			SettingsManager settingsManager,
			LifecycleManager lifecycleManager) {
		AnimatedEmojiManager manager = new AnimatedEmojiManager(
				app, dbExecutor, androidExecutor, settingsManager);
		lifecycleManager.registerOpenDatabaseHook(manager);
		return manager;
	}

	@Provides
	@Singleton
	MessageReactionManager provideMessageReactionManager(
			@DatabaseExecutor Executor dbExecutor,
			AndroidExecutor androidExecutor,
			SettingsManager settingsManager,
			LifecycleManager lifecycleManager) {
		MessageReactionManager manager = new MessageReactionManager(
				dbExecutor, androidExecutor, settingsManager);
		lifecycleManager.registerOpenDatabaseHook(manager);
		return manager;
	}
}
