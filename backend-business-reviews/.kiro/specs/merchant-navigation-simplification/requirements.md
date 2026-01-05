# Requirements Document

## Introduction

本需求文档描述商家运营中心Web端导航栏的简化和重组。当前系统包含数据看板、UniApp对接等功能模块,需要移除这些模块,并重新组织核心业务功能的导航结构,使界面更加简洁、聚焦于核心商家运营功能。

## Glossary

- **Merchant Center**: 商家运营中心,商家管理其业务的Web管理后台
- **Navigation Sidebar**: 导航侧边栏,位于页面左侧的主导航菜单
- **Dashboard**: 数据看板,展示统计数据和图表的页面
- **UniApp Integration**: UniApp对接功能,包含用户管理、消息通知、数据分析等子模块
- **Core Business Modules**: 核心业务模块,包括门店管理、笔记管理、优惠券、评论管理、系统设置

## Requirements

### Requirement 1

**User Story:** 作为商家用户,我希望移除数据看板功能,以便界面更加简洁,聚焦于核心业务管理。

#### Acceptance Criteria

1. WHEN the Merchant Center loads THEN the Navigation Sidebar SHALL NOT display the Dashboard menu item
2. WHEN a user attempts to access the Dashboard route directly THEN the system SHALL redirect to the first available menu page
3. WHEN the Navigation Sidebar renders THEN the system SHALL maintain proper spacing and layout without the Dashboard item

### Requirement 2

**User Story:** 作为商家用户,我希望移除UniApp对接相关功能模块,以便简化系统功能,专注于核心业务。

#### Acceptance Criteria

1. WHEN the Merchant Center loads THEN the Navigation Sidebar SHALL NOT display the UniApp Integration submenu
2. WHEN the Navigation Sidebar renders THEN the system SHALL NOT display User Management, Message Notification, or Data Analysis menu items
3. WHEN a user attempts to access any UniApp Integration route directly THEN the system SHALL redirect to the first available menu page

### Requirement 3

**User Story:** 作为商家用户,我希望核心业务模块(门店管理、笔记管理、优惠券、评论管理、系统设置)保持可访问,以便继续使用这些功能。

#### Acceptance Criteria

1. WHEN the Merchant Center loads THEN the Navigation Sidebar SHALL display Shop Management as a menu item
2. WHEN the Merchant Center loads THEN the Navigation Sidebar SHALL display Note Management as a menu item
3. WHEN the Merchant Center loads THEN the Navigation Sidebar SHALL display Coupon Management as a menu item
4. WHEN the Merchant Center loads THEN the Navigation Sidebar SHALL display Comment Management as a menu item
5. WHEN the Merchant Center loads THEN the Navigation Sidebar SHALL display System Settings as a menu item

### Requirement 4

**User Story:** 作为商家用户,我希望导航菜单按照逻辑顺序排列,以便快速找到需要的功能。

#### Acceptance Criteria

1. WHEN the Navigation Sidebar renders THEN the menu items SHALL be ordered as: Shop Management, Note Management, Coupon Management, Comment Management, System Settings
2. WHEN the Navigation Sidebar renders THEN each menu item SHALL maintain consistent styling and spacing
3. WHEN the Navigation Sidebar renders THEN the active menu item SHALL be visually highlighted

### Requirement 5

**User Story:** 作为商家用户,我希望移除顶部导航栏中的UniApp同步状态显示,以便界面更加简洁。

#### Acceptance Criteria

1. WHEN the Header renders THEN the system SHALL NOT display the UniApp sync status indicator
2. WHEN the Header renders THEN the system SHALL maintain proper spacing and alignment of remaining header elements
3. WHEN the Header renders THEN the notification badge and user dropdown SHALL remain functional

### Requirement 6

**User Story:** 作为开发者,我希望路由配置与导航菜单保持一致,以便系统正常运行。

#### Acceptance Criteria

1. WHEN the application initializes THEN the router SHALL define routes only for Shop Management, Note Management, Coupon Management, Comment Management, and System Settings
2. WHEN a user navigates to a removed route THEN the system SHALL redirect to the default page (Shop Management)
3. WHEN the router configuration updates THEN the system SHALL maintain proper route guards and navigation behavior
