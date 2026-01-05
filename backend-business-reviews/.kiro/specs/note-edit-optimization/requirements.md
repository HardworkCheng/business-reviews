# Requirements Document

## Introduction

This specification addresses three critical issues with the note editing functionality in the mobile application:
1. The publish time does not update after editing a note
2. Topics are not displayed when editing a note (topic field回显失败)
3. The share and edit buttons need UI optimization and proper permission control

## Glossary

- **Note System**: The system component responsible for managing user-generated content including text, images, topics, and location
- **Topic**: A categorization tag that users can associate with notes, stored in the topics table
- **Note-Topic Relationship**: The many-to-many relationship between notes and topics, stored in the note_topics table
- **Publish Time**: The relative time display showing when a note was last updated (e.g., "23分钟前")
- **Author Permission**: The access control that ensures only the note creator can edit their own notes

## Requirements

### Requirement 1

**User Story:** As a user, I want to see the updated time after editing my note, so that I know when the note was last modified.

#### Acceptance Criteria

1. WHEN a user successfully updates a note THEN the system SHALL update the updated_at timestamp in the database
2. WHEN a user views a note detail page THEN the system SHALL display the relative time based on the updated_at field
3. WHEN the note detail page loads THEN the system SHALL format the time correctly showing minutes, hours, or days ago

### Requirement 2

**User Story:** As a user, I want to see the topics I previously selected when editing my note, so that I can modify or keep them as needed.

#### Acceptance Criteria

1. WHEN a user opens the note edit page THEN the system SHALL query the note_topics table for associated topics
2. WHEN the backend returns note details THEN the system SHALL include the complete topic list with id and name fields
3. WHEN the edit page loads THEN the system SHALL display all associated topics in the selectedTopics array
4. WHEN a user updates a note with topics THEN the system SHALL delete old topic associations and create new ones

### Requirement 3

**User Story:** As a user, I want clean button styling and proper edit permissions, so that the interface is clear and only I can edit my own notes.

#### Acceptance Criteria

1. WHEN the note detail page renders THEN the system SHALL remove background colors from share and edit buttons
2. WHEN a non-author views a note THEN the system SHALL hide the edit button completely
3. WHEN the note author views their own note THEN the system SHALL display the edit button
4. WHEN the backend returns note details THEN the system SHALL include an isAuthor boolean field indicating ownership
5. WHEN the frontend receives note data THEN the system SHALL use the isAuthor field to control edit button visibility
