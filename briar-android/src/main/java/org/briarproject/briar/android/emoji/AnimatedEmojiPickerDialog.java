package org.briarproject.briar.android.emoji;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

import org.briarproject.briar.R;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
@NotNullByDefault
public class AnimatedEmojiPickerDialog extends DialogFragment {

	public interface EmojiSelectionListener {
		void onEmojiSelected(AnimatedEmoji emoji);
	}

	private AnimatedEmojiManager emojiManager;
	private EmojiSelectionListener listener;
	private EmojiAdapter adapter;
	private List<AnimatedEmoji> currentEmojis = new ArrayList<>();

	public static AnimatedEmojiPickerDialog newInstance(AnimatedEmojiManager manager,
			EmojiSelectionListener listener) {
		AnimatedEmojiPickerDialog dialog = new AnimatedEmojiPickerDialog();
		dialog.emojiManager = manager;
		dialog.listener = listener;
		return dialog;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		Context context = requireContext();
		View view = LayoutInflater.from(context)
				.inflate(R.layout.dialog_animated_emoji_picker, null);

		TabLayout tabLayout = view.findViewById(R.id.categoryTabs);
		RecyclerView recyclerView = view.findViewById(R.id.emojiRecyclerView);

		recyclerView.setLayoutManager(new GridLayoutManager(context, 4));
		adapter = new EmojiAdapter(context);
		recyclerView.setAdapter(adapter);

		List<String> categories = emojiManager.getCategories();
		for (String category : categories) {
			TabLayout.Tab tab = tabLayout.newTab();
			tab.setText(category.replace("_", " ").toUpperCase());
			tab.setTag(category);
			tabLayout.addTab(tab);
		}

		if (!categories.isEmpty()) {
			loadCategory(categories.get(0));
		}

		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				String category = (String) tab.getTag();
				if (category != null) {
					loadCategory(category);
				}
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {}
		});

		return new MaterialAlertDialogBuilder(context)
				.setView(view)
				.setNegativeButton(android.R.string.cancel, null)
				.create();
	}

	private void loadCategory(String category) {
		currentEmojis.clear();
		currentEmojis.addAll(emojiManager.getEmojisByCategory(category));
		adapter.notifyDataSetChanged();
	}

	private class EmojiAdapter extends RecyclerView.Adapter<EmojiViewHolder> {

		private final Context context;

		EmojiAdapter(Context context) {
			this.context = context;
		}

		@NonNull
		@Override
		public EmojiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(context)
					.inflate(R.layout.list_item_animated_emoji, parent, false);
			return new EmojiViewHolder(view);
		}

		@Override
		public void onBindViewHolder(@NonNull EmojiViewHolder holder, int position) {
			AnimatedEmoji emoji = currentEmojis.get(position);
			holder.bind(emoji);
		}

		@Override
		public int getItemCount() {
			return currentEmojis.size();
		}
	}

	private class EmojiViewHolder extends RecyclerView.ViewHolder {

		private final LottieAnimationView animationView;
		private final TextView nameView;

		EmojiViewHolder(View itemView) {
			super(itemView);
			animationView = itemView.findViewById(R.id.emojiAnimation);
			nameView = itemView.findViewById(R.id.emojiName);
		}

		void bind(AnimatedEmoji emoji) {
			nameView.setText(emoji.getName());
			try {
				animationView.setAnimation(emoji.getAssetPath());
				animationView.playAnimation();
			} catch (Exception e) {
			}

			itemView.setOnClickListener(v -> {
				if (listener != null) {
					listener.onEmojiSelected(emoji);
				}
				dismiss();
			});
		}
	}
}
