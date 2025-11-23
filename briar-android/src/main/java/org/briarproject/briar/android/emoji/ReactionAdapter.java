package org.briarproject.briar.android.emoji;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.briarproject.briar.R;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
@NotNullByDefault
public class ReactionAdapter extends RecyclerView.Adapter<ReactionAdapter.ReactionViewHolder> {

	private final Context context;
	private final List<ReactionGroup> reactions = new ArrayList<>();

	public ReactionAdapter(Context context) {
		this.context = context;
	}

	public void setReactions(List<MessageReaction> messageReactions) {
		Map<String, Integer> reactionCounts = new HashMap<>();
		for (MessageReaction reaction : messageReactions) {
			String emoji = reaction.getEmoji();
			int count = reactionCounts.containsKey(emoji) ? 
				reactionCounts.get(emoji) : 0;
			reactionCounts.put(emoji, count + 1);
		}

		reactions.clear();
		for (Map.Entry<String, Integer> entry : reactionCounts.entrySet()) {
			reactions.add(new ReactionGroup(entry.getKey(), entry.getValue()));
		}
		notifyDataSetChanged();
	}

	@NonNull
	@Override
	public ReactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context)
				.inflate(R.layout.list_item_reaction, parent, false);
		return new ReactionViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ReactionViewHolder holder, int position) {
		holder.bind(reactions.get(position));
	}

	@Override
	public int getItemCount() {
		return reactions.size();
	}

	static class ReactionViewHolder extends RecyclerView.ViewHolder {

		private final TextView emojiView;
		private final TextView countView;

		ReactionViewHolder(View itemView) {
			super(itemView);
			emojiView = itemView.findViewById(R.id.reactionEmoji);
			countView = itemView.findViewById(R.id.reactionCount);
		}

		void bind(ReactionGroup reaction) {
			emojiView.setText(reaction.emoji);
			countView.setText(String.valueOf(reaction.count));
		}
	}

	private static class ReactionGroup {
		final String emoji;
		final int count;

		ReactionGroup(String emoji, int count) {
			this.emoji = emoji;
			this.count = count;
		}
	}
}
