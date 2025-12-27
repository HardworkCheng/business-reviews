# Implementation Plan

- [x] 1. Fix backend topic update logic
  - Update NoteServiceImpl.updateNote() to handle topic associations
  - Add logic to delete existing note_topics entries
  - Add logic to insert new note_topics entries based on request
  - Ensure transaction rollback on failure
  - _Requirements: 2.4_

- [x] 2. Verify backend getNoteDetail returns topics correctly
  - Check that getNoteTopics() is called in getNoteDetail()
  - Verify TopicInfo objects are properly constructed with id and name
  - Ensure topics list is included in NoteDetailVO response
  - Test API endpoint returns topics in JSON response
  - _Requirements: 2.1, 2.2_

- [x] 3. Fix frontend topic display in note-edit page
  - Update fetchNoteDetail() in note-edit.vue to properly map topics
  - Ensure result.topics is correctly assigned to selectedTopics.value
  - Verify topic objects have both id and name properties
  - Add console logging to debug topic loading
  - _Requirements: 2.3_

- [x] 4. Fix time display to show updated_at
  - Verify backend returns updated_at in createdAt field (or add updatedAt field)
  - Update note-detail.vue to use the correct timestamp field
  - Ensure formatTime() function is called with updated timestamp
  - Test that time updates after editing a note
  - _Requirements: 1.1, 1.2, 1.3_

- [x] 5. Remove button background styling and fix edit permissions
  - Remove background classes from edit and share buttons in note-detail.vue
  - Update button styling to remove any background colors
  - Verify isAuthor field is returned from backend (currently named selfAuthor)
  - Update frontend to use noteData.isAuthor for edit button visibility
  - Ensure edit button only shows for note author
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

## BUG FIXES (New Issues Found)

- [x] 6. Fix time display showing "刚刚" on refresh
  - **Issue**: When refreshing note detail page, time always shows "刚刚" (just now) instead of actual edit time
  - **Root Cause**: Backend was returning createdAt instead of updatedAt in the publishTime field
  - **Fix**: Updated backend NoteServiceImpl.getNoteDetail() line 237 to use updatedAt in publishTime field
  - **Status**: ✅ FIXED - Backend now returns `TimeUtil.formatRelativeTime(note.getUpdatedAt() != null ? note.getUpdatedAt() : note.getCreatedAt())`
  - _Requirements: 1.1, 1.2, 1.3_

- [ ] 7. Fix topic not displaying in edit page
  - **Issue**: When editing a note, topics are not showing in the edit form
  - **Root Cause**: Database missing topic data - topics table and note_topics table need initialization
  - **Fix Steps**:
    1. Execute `backend-business-reviews/sql/fix_topic_display.sql` to initialize topics
    2. Add topic associations for test notes
    3. Verify API returns topics correctly
    4. Test frontend display
  - **Status**: ⚠️ IN PROGRESS - SQL script created, needs execution
  - **Guide**: See `.kiro/specs/note-edit-optimization/TOPIC_FIX_GUIDE.md` for detailed steps
  - _Requirements: 2.1, 2.2, 2.3_

- [ ] 8. Final testing and verification
  - Test complete edit flow with topics
  - Verify time updates correctly after edit and shows correct time on refresh
  - Test topic display in edit page
  - Test on actual device/simulator
  - _Requirements: All_
