# Implementation Plan

- [x] 1. Update MainLayout component to remove Dashboard and UniApp menu items


  - Modify `src/layouts/MainLayout.vue` to remove Dashboard menu item from sidebar
  - Remove UniApp Integration submenu and all its child items (User Management, Message Notification, Data Analysis)
  - Remove UniApp sync status indicator from Header component
  - Ensure menu items are ordered correctly: Shop Management, Note Management, Coupon Management, Comment Management, System Settings
  - Update computed properties and reactive data if needed
  - _Requirements: 1.1, 1.3, 2.1, 2.2, 3.1, 3.2, 3.3, 3.4, 3.5, 4.1, 4.2, 5.1, 5.2_

- [ ]* 1.1 Write unit tests for MainLayout component
  - Test that Dashboard menu item is not rendered
  - Test that UniApp submenu is not rendered
  - Test that all core business menu items are present
  - Test that menu items appear in correct order
  - Test that UniApp sync status is not in Header
  - _Requirements: 1.1, 2.1, 2.2, 3.1-3.5, 4.1, 5.1_



- [ ] 2. Update router configuration to remove Dashboard and UniApp routes
  - Modify `src/router/index.ts` to remove Dashboard route import and configuration
  - Remove UniApp related routes (users, messages, analytics)
  - Change root path redirect from Dashboard to Shop Management (/shops)
  - Add route guard to redirect removed routes to /shops
  - Ensure all core business routes remain intact
  - _Requirements: 1.2, 2.3, 6.1, 6.2, 6.3_

- [ ]* 2.1 Write unit tests for router configuration
  - Test that Dashboard route does not exist in router
  - Test that UniApp routes do not exist in router
  - Test that core business routes are properly configured


  - Test route guard redirects removed routes to /shops
  - _Requirements: 1.2, 2.3, 6.1, 6.2_

- [ ] 3. Test navigation flow and verify all functionality
  - Manually test that application loads and redirects to /shops by default
  - Test that all core menu items are clickable and navigate correctly
  - Test that attempting to access removed routes redirects to /shops
  - Test sidebar collapse/expand functionality


  - Test breadcrumb navigation
  - Test user dropdown menu
  - Verify responsive layout on different screen sizes
  - _Requirements: All requirements_

- [ ] 4. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ]* 5. Optional cleanup of unused files
  - Delete `src/views/dashboard/index.vue` if no longer needed
  - Delete `src/views/user/list.vue` if no longer needed
  - Delete `src/views/message/list.vue` if no longer needed
  - Delete `src/views/analytics/index.vue` if no longer needed
  - Delete related API files if they exist and are no longer used
  - Update any documentation that references removed features
  - _Requirements: N/A (cleanup task)_
