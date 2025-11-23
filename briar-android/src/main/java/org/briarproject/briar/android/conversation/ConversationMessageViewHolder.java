package org.briarproject.briar.android.conversation;

import android.content.res.ColorStateList;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import org.briarproject.briar.R;
import org.briarproject.briar.android.attachment.AttachmentItem;
import org.briarproject.nullsafety.NotNullByDefault;

import androidx.annotation.UiThread;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.RecycledViewPool;

import static androidx.constraintlayout.widget.ConstraintSet.WRAP_CONTENT;
import static androidx.core.content.ContextCompat.getColor;
import static androidx.core.widget.ImageViewCompat.setImageTintList;

@UiThread
@NotNullByDefault
class ConversationMessageViewHolder extends ConversationItemViewHolder {

        private final ImageAdapter adapter;
        private final ViewGroup statusLayout;
        private final RecyclerView reactionsRecyclerView;
        private final org.briarproject.briar.android.emoji.ReactionAdapter reactionAdapter;
        private final int timeColor, timeColorBubble;
        private final ConstraintSet textConstraints = new ConstraintSet();
        private final ConstraintSet imageConstraints = new ConstraintSet();
        private final ConstraintSet imageTextConstraints = new ConstraintSet();
        private final GestureDetector gestureDetector;

        ConversationMessageViewHolder(View v, ConversationListener listener,
                        boolean isIncoming, RecycledViewPool imageViewPool,
                        ImageItemDecoration imageItemDecoration) {
                super(v, listener, isIncoming);
                statusLayout = v.findViewById(R.id.statusLayout);
                reactionsRecyclerView = v.findViewById(R.id.reactionsRecyclerView);

                // setup reactions adapter
                if (reactionsRecyclerView != null) {
                        reactionAdapter = new org.briarproject.briar.android.emoji.ReactionAdapter(
                                        v.getContext());
                        reactionsRecyclerView.setAdapter(reactionAdapter);
                } else {
                        reactionAdapter = null;
                }

                // image list
                RecyclerView list = v.findViewById(R.id.imageList);
                list.setRecycledViewPool(imageViewPool);
                adapter = new ImageAdapter(v.getContext(), listener);
                list.setAdapter(adapter);
                list.addItemDecoration(imageItemDecoration);

                // remember original status text color
                timeColor = time.getCurrentTextColor();
                timeColorBubble =
                                getColor(v.getContext(), R.color.msg_status_bubble_foreground);

                // clone constraint sets from layout files
                textConstraints.clone(v.getContext(),
                                R.layout.list_item_conversation_msg_in_content);
                imageConstraints.clone(v.getContext(),
                                R.layout.list_item_conversation_msg_image);
                imageTextConstraints.clone(v.getContext(),
                                R.layout.list_item_conversation_msg_image_text);

                // in/out are different layouts, so we need to do this only once
                textConstraints
                                .setHorizontalBias(R.id.statusLayout, isIncoming() ? 1 : 0);
                imageConstraints
                                .setHorizontalBias(R.id.statusLayout, isIncoming() ? 1 : 0);
                imageTextConstraints
                                .setHorizontalBias(R.id.statusLayout, isIncoming() ? 1 : 0);

                // setup double tap gesture detector
                gestureDetector = new GestureDetector(v.getContext(),
                                new GestureDetector.SimpleOnGestureListener() {
                                        @Override
                                        public boolean onDoubleTap(MotionEvent e) {
                                                String key = getItemKey();
                                                if (key != null) {
                                                        ConversationItem item = getCurrentItem();
                                                        if (item instanceof ConversationMessageItem) {
                                                                listener.onMessageDoubleTap(
                                                                                (ConversationMessageItem) item);
                                                                return true;
                                                        }
                                                }
                                                return false;
                                        }
                                });

                layout.setOnTouchListener((view, event) -> {
                        gestureDetector.onTouchEvent(event);
                        return false;
                });
        }

        private ConversationItem currentItem = null;

        private ConversationItem getCurrentItem() {
                return currentItem;
        }

        @Override
        void bind(ConversationItem conversationItem, boolean selected) {
                super.bind(conversationItem, selected);
                currentItem = conversationItem;
                ConversationMessageItem item =
                                (ConversationMessageItem) conversationItem;
                if (item.getAttachments().isEmpty()) {
                        bindTextItem();
                } else {
                        bindImageItem(item);
                }
        }

        private void bindTextItem() {
                resetStatusLayoutForText();
                textConstraints.applyTo(layout);
                adapter.clear();
        }

        private void bindImageItem(ConversationMessageItem item) {
                ConstraintSet constraintSet;
                if (item.getText() == null) {
                        statusLayout.setBackgroundResource(R.drawable.msg_status_bubble);
                        time.setTextColor(timeColorBubble);
                        setImageTintList(bomb, ColorStateList.valueOf(timeColorBubble));
                        constraintSet = imageConstraints;
                } else {
                        resetStatusLayoutForText();
                        constraintSet = imageTextConstraints;
                }

                if (item.getAttachments().size() == 1) {
                        // apply image size constraints for a single image
                        AttachmentItem attachment = item.getAttachments().get(0);
                        int width = attachment.getThumbnailWidth();
                        int height = attachment.getThumbnailHeight();
                        constraintSet.constrainWidth(R.id.imageList, width);
                        constraintSet.constrainHeight(R.id.imageList, height);
                } else {
                        // bubble adapts to size of image list
                        constraintSet.constrainWidth(R.id.imageList, WRAP_CONTENT);
                        constraintSet.constrainHeight(R.id.imageList, WRAP_CONTENT);
                }
                constraintSet.applyTo(layout);
                adapter.setConversationItem(item);
        }

        private void resetStatusLayoutForText() {
                statusLayout.setBackgroundResource(0);
                // also reset padding (the background drawable defines some)
                statusLayout.setPadding(0, 0, 0, 0);
                time.setTextColor(timeColor);
                setImageTintList(bomb, ColorStateList.valueOf(timeColor));
        }

}
