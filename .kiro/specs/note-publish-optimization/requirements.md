# Requirements Document

## Introduction

本文档定义了发布笔记功能的优化需求。主要包括：关联商户功能增强（支持选择已注册商户）、话题功能完善（添加和显示）、位置功能优化（支持定位选择和显示）、删除不必要的功能（标签、公开设置、表情和@功能），以及页面美化。

## Glossary

- **Note（笔记）**: 用户发布的图文内容，包含标题、内容、图片、位置等信息
- **Shop（商户）**: 在商家运营中心注册的商家实体
- **Topic（话题）**: 用于分类和聚合笔记的标签，以#开头显示
- **Location（位置）**: 用户发布笔记时的地理位置信息
- **Merchant（商家运营中心）**: 商家管理后台系统
- **PublishNoteRequest**: 发布笔记的请求数据结构
- **NoteDetailResponse**: 笔记详情的响应数据结构

## Requirements

### Requirement 1

**User Story:** As a user, I want to associate my note with a registered merchant, so that readers can discover the merchant through my note.

#### Acceptance Criteria

1. WHEN a user clicks the "关联商户" option THEN the system SHALL display a searchable list of merchants registered in the merchant operation center
2. WHEN a user selects a merchant from the list THEN the system SHALL store the merchant association with the note
3. WHEN a note with associated merchant is displayed THEN the system SHALL show the merchant name with a clickable link to the merchant detail page
4. WHEN the user views note detail THEN the system SHALL display the associated merchant information in a dedicated section

### Requirement 2

**User Story:** As a user, I want to add topics to my note, so that my note can be discovered through topic searches.

#### Acceptance Criteria

1. WHEN a user clicks the "添加话题" option THEN the system SHALL display a list of available topics for selection
2. WHEN a user selects topics THEN the system SHALL allow multiple topic selection (up to 5 topics)
3. WHEN topics are selected THEN the system SHALL display selected topics with # prefix in the publish page
4. WHEN a note with topics is displayed THEN the system SHALL show topics as clickable tags with # prefix in the note detail page
5. WHEN the note is published THEN the system SHALL persist the topic associations to the database

### Requirement 3

**User Story:** As a user, I want to add my current location to the note, so that readers know where the content was created.

#### Acceptance Criteria

1. WHEN a user clicks the "添加位置" option THEN the system SHALL navigate to a location picker page with map
2. WHEN the location picker opens THEN the system SHALL attempt to get the user's current GPS location
3. WHEN a user selects a location THEN the system SHALL store the location name, latitude, and longitude
4. WHEN a note with location is displayed THEN the system SHALL show the location name with a location icon in the note detail page
5. WHEN the user clicks the location in note detail THEN the system SHALL navigate to a map view showing the location

### Requirement 4

**User Story:** As a developer, I want to remove unused features from the publish page, so that the interface is cleaner and simpler.

#### Acceptance Criteria

1. WHEN the publish page loads THEN the system SHALL NOT display the "添加标签" option
2. WHEN the publish page loads THEN the system SHALL NOT display the "公开" visibility option
3. WHEN the publish page loads THEN the system SHALL NOT display the emoji button in the footer toolbar
4. WHEN the publish page loads THEN the system SHALL NOT display the @ mention button in the footer toolbar
5. WHEN a note is published THEN the system SHALL set the note status to public (status=1) by default

### Requirement 5

**User Story:** As a user, I want a clean and well-organized publish page, so that I can easily create and publish notes.

#### Acceptance Criteria

1. WHEN the publish page loads THEN the system SHALL display a clean layout with proper spacing between sections
2. WHEN the publish page loads THEN the system SHALL display consistent styling for all option items
3. WHEN the publish page loads THEN the system SHALL display clear visual hierarchy with proper font sizes and colors
4. WHEN the user interacts with the page THEN the system SHALL provide smooth transitions and feedback
5. WHEN the user views selected items (merchant, topics, location) THEN the system SHALL display them with clear visual indicators

