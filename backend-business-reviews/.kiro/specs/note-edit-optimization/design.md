# Design Document

## Overview

This design addresses three interconnected issues in the note editing system:
1. Time display not updating after edits
2. Topics not being loaded/displayed during edit
3. UI styling and permission control for edit/share buttons

The solution involves backend API enhancements, frontend data handling improvements, and UI styling updates.

## Architecture

### Component Interaction Flow

```
User Action (Edit Note)
    ↓
Frontend (note-edit.vue)
    ↓
API Layer (note.js)
    ↓
Backend Controller (NoteController)
    ↓
Service Layer (NoteServiceImpl)
    ↓
Database (notes, note_topics, topics tables)
```

## Components and Interfaces

### Backend Components

#### 1. NoteServiceImpl.updateNote()
- **Current Issue**: Does not update topic associations
- **Enhancement**: Add topic update logic similar to publishNote()
- **Location**: `backend-business-reviews-service/src/main/java/com/businessreviews/service/impl/app/NoteServiceImpl.java`

#### 2. NoteServiceImpl.getNoteDetail()
- **Current Status**: Already returns topics via getNoteTopics()
- **Verification Needed**: Ensure topics are properly serialized in response

### Frontend Components

#### 1. note-edit.vue
- **Current Issue**: Topics not populated from API response
- **Fix**: Properly map topics from result.topics to selectedTopics
- **Location**: `front-business-reviews-Mobile/src/pages/note-edit/note-edit.vue`

#### 2. note-detail.vue
- **Current Issue**: Time display uses publishTime instead of updated_at
- **Fix**: Use formatTime(noteData.createdAt) which should use updated_at
- **UI Issue**: Edit/share buttons have background styling
- **Fix**: Remove background classes and add conditional rendering for edit button

## Data Models

### Database Schema

#### notes table (existing)
```sql
updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
```
- This field automatically updates on any UPDATE operation

#### note_topics table (existing)
```sql
CREATE TABLE note_topics (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  note_id BIGINT NOT NULL,
  topic_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
)
```

#### topics table (existing)
```sql
CREATE TABLE topics (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  ...
)
```

### API Response Models

#### NoteDetailVO (existing, needs verification)
```java
public class NoteDetailVO {
    private Long id;
    private String title;
    private String content;
    private List<String> images;
    private List<TopicInfo> topics;  // ← Must be populated
    private LocalDateTime createdAt;  // ← Should reflect updated_at
    private Boolean isAuthor;  // ← Already exists as "selfAuthor"
    ...
    
    public static class TopicInfo {
        private Long id;
        private String name;
    }
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Time Update Consistency
*For any* note update operation, the updated_at timestamp in the database should be automatically updated to the current time, and this timestamp should be reflected in the API response.
**Validates: Requirements 1.1, 1.2**

### Property 2: Topic Association Preservation
*For any* note with associated topics, when the note edit page loads, all topic associations should be retrieved from the database and displayed in the UI.
**Validates: Requirements 2.1, 2.2, 2.3**

### Property 3: Topic Update Atomicity
*For any* note update with topic changes, the system should delete all existing topic associations and create new ones within a single transaction.
**Validates: Requirements 2.4**

### Property 4: Edit Permission Enforcement
*For any* note detail view, the edit button should only be visible when the current user is the note author.
**Validates: Requirements 3.2, 3.3, 3.5**

### Property 5: Button Styling Consistency
*For any* note detail page render, the share and edit buttons should not have background color styling applied.
**Validates: Requirements 3.1**

## Error Handling

### Backend Errors
- **Topic Not Found**: If a topic ID doesn't exist, log warning and skip that topic
- **Permission Denied**: Return 403 if non-author attempts to edit
- **Database Transaction Failure**: Rollback all changes if topic update fails

### Frontend Errors
- **API Failure**: Show toast message "加载失败，请重试"
- **Empty Topics**: Handle gracefully with empty array
- **Missing isAuthor Field**: Default to false for safety

## Testing Strategy

### Unit Tests
- Test updateNote() method updates topics correctly
- Test getNoteDetail() returns complete topic list
- Test time formatting function with various timestamps
- Test edit button visibility logic

### Property-Based Tests
- Property 1: Verify updated_at changes after any update operation
- Property 2: Verify topics array is never null in API response
- Property 3: Verify topic associations match database after update
- Property 4: Verify edit button visibility matches isAuthor field
- Property 5: Verify button elements don't have background classes

### Integration Tests
- Test complete edit flow: load → modify topics → save → verify
- Test time display updates after edit
- Test non-author cannot see edit button
- Test author can see and use edit button

### Manual Testing Checklist
1. Edit a note and verify time updates on detail page
2. Edit a note with topics and verify they appear in edit form
3. Add/remove topics and verify changes persist
4. View own note and verify edit button appears
5. View another user's note and verify no edit button
6. Verify share/edit buttons have no background color
