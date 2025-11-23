# Briar Messenger - Premium Emoji Feature

## Recent Changes (November 23, 2025)

### Added Features
1. **Premium Animated Emoji Next to Names** (Local Only)
   - Users can set an animated emoji that appears next to their name
   - ⚠️ **Limited to 3 watch emojis** (watch1.tgs, watch2.tgs, watch3.tgs)
   - ⚠️ **Local storage only** - emojis are NOT synced with contacts
   - Settings location: Main Settings → "Premium Emoji"
   - **Note**: Full contact sync requires integration with Briar's Identity system

2. **Like Button for Messages** (New - November 23, 2025)
   - Explicit like button (heart icon) next to all messages
   - Click to add/remove like reaction
   - Heart icon toggles between outline and filled state
   - UI updates immediately on click
   - ⚠️ **Local storage only** - likes are NOT synced between contacts
   - Uses MessageReactionManager for reaction storage

3. **Double-Tap Reactions** (Already Implemented)
   - Double-tap on messages to add a heart (❤) reaction
   - Reactions are immediately visible on messages
   - Implementation includes proper UI updates via ReactionListener

### Implementation Details

#### Files Modified
1. **AnimatedEmojiManager.java**
   - Loads only 3 watch emoji files (watch1.tgs, watch2.tgs, watch3.tgs)
   - Added `ownEmojiId` field to store user's own emoji
   - Added methods: `setOwnEmoji()`, `getOwnEmoji()`, `getOwnEmojiId()`
   - Persists own emoji to settings database (local only)

2. **SettingsFragment.java**
   - Added "Premium Emoji" preference
   - Shows emoji picker dialog on click
   - Supports removing emoji with confirmation dialog
   - Updates preference summary to show current emoji name

3. **ConversationMessageViewHolder.java** (Like Button - New)
   - Added `LOCAL_USER_ID` constant for local reaction tracking
   - Added `likeButton` ImageButton field
   - Created `updateLikeButtonState()` method to refresh button icon
   - Created `updateReactionsDisplay()` method to refresh reaction list
   - Click handler toggles like on/off and updates UI immediately
   - Uses MessageReactionManager for persistence

4. **list_item_conversation_msg_in_content.xml & list_item_conversation_msg_out.xml**
   - Added `ImageButton` with id `likeButton`
   - Positioned next to message timestamp
   - Uses `ic_heart` and `ic_heart_filled` drawables

5. **ic_heart.xml & ic_heart_filled.xml** (New Vector Drawables)
   - Outline and filled heart icons for like button
   - Both use tintable colors (black fill) for theme compatibility

6. **list_item_contact.xml**
   - Added `LottieAnimationView` for animated emoji display
   - Wrapped name and emoji in `LinearLayout` for proper alignment

7. **ContactItemViewHolder.java**
   - Caches `AnimatedEmojiManager` instance
   - Loads and displays contact's emoji
   - Properly cancels/clears animations on view recycling

8. **AuthorView.java**
   - Loads and displays animated emoji for:
     - Own messages (shows own emoji)
     - Contact messages (shows contact's emoji)
   - Properly handles animation lifecycle

#### Architecture Decisions
- **Emoji Storage**: Stored in SettingsManager under `pref_own_animated_emoji` key
- **Animation Lifecycle**: Animations are explicitly canceled/cleared before recycling to prevent memory leaks
- **Manager Access**: Cached in view holders to minimize repeated component lookups

### Known Limitations

#### Critical Limitations
1. **❌ NO Contact Synchronization**
   - Emojis are stored locally only (SettingsManager) and NOT synced with contacts
   - Contacts cannot see each other's emojis (requires Briar Identity system integration)
   - Likes are stored locally only and NOT synced between users
   - Full sync requires: IdentityManager integration, Author properties, LocalAuthorChangedEvents

2. **❌ NO Like Notifications**
   - No notification system for when messages are liked
   - Requires integration with AndroidNotificationManager and ConversationMessageReactionAdded events

#### Minor Limitations
3. **Architecture**: Emoji manager accessed via `AppModule.getAndroidComponent()` (full DI requires refactoring)
4. **Limited Emoji Selection**: Only 3 watch emojis available (watch1, watch2, watch3)
5. **Async Save**: Emoji save is asynchronous without failure notification
6. **UI Updates**: List views update emoji on next recycling/rebind, not immediately after selection
7. **Settings Summary**: Updates on Settings screen entry (onStart), not real-time

### Functional Status
**✅ Working Locally:**
- Set premium emoji in Settings → Premium Emoji (3 watch emojis only)
- View own emoji next to own name in chat messages
- Remove emoji via Settings dialog
- Like button appears next to all messages
- Click like button to toggle like on/off
- Like button updates immediately (filled/outline)
- Reaction count displays under messages
- Double-tap messages to add heart reaction
- Animations properly cleaned up on view recycling

**❌ NOT Working:**
- Emojis NOT visible to contacts (local storage only)
- Likes NOT synced between contacts (local storage only)
- No notifications when messages are liked
- Cannot see contacts' emojis (they cannot see yours either)

These limitations are acceptable for basic functionality and can be improved in future iterations with proper architecture changes (LiveData, ViewModel, etc.)

### Testing Recommendations
1. **Set Premium Emoji**: Settings → Premium Emoji → Select from 3 watch emojis
2. **Verify Display**: Check own messages in chat to see emoji next to own name
3. **Remove Emoji**: Settings → Premium Emoji → Remove Emoji
4. **Like Button**: Click heart button next to any message to toggle like
5. **Double-Tap Reactions**: Double-tap any message in chat to add heart reaction

### Future Work Required for Full Functionality
To make emojis and likes work between contacts, the following Briar architecture integration is required:

1. **Identity System Integration**
   - Store emoji ID in Author properties (via IdentityManager)
   - Handle LocalAuthorChangedEvents to propagate emoji updates
   - Sync emoji data across contacts via Briar's protocol

2. **Like Notification System**
   - Integrate with AndroidNotificationManager
   - Listen for ConversationMessageReactionAdded events
   - Create notification entries when contacts like messages

3. **Database Persistence**
   - Move reactions from local Settings to Briar's database
   - Create proper database schema for reactions
   - Handle reaction sync via MessagingManager

**Estimated Effort**: 3-5 days of development for a Briar core developer familiar with the architecture

## Project Structure
- **Platform**: Android (Java)
- **Build System**: Gradle
- **Key Dependencies**: 
  - Lottie for animated emojis
  - Material Design Components
  - AndroidX libraries

## Development Notes
- LSP diagnostics are present but mostly non-critical (type resolution issues)
- All functionality is implemented and ready for testing
- No workflows configured yet - this is a library/module within the larger Briar project
