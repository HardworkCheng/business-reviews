# Requirements Document

## Introduction

本功能旨在优化商家运营中心的店铺信息管理页面，主要包括：删除图片上传相关功能、添加店铺信息修改按钮、优化地址输入体验（支持自动填充和手动输入省市区）、根据地址自动计算经纬度，以及修复首页访问时未跳转到登录页的问题。

## Glossary

- **Shop_Info_Page**: 店铺信息管理页面，商家用于管理店铺基本信息的界面
- **Address_Autocomplete**: 地址自动填充功能，根据输入的地址自动解析省份、城市、区县
- **Geocoding**: 地理编码，将地址转换为经纬度坐标的过程
- **Login_Redirect**: 登录重定向，未登录用户访问受保护页面时自动跳转到登录页
- **Edit_Button**: 修改按钮，用于触发店铺信息编辑模式的按钮

## Requirements

### Requirement 1

**User Story:** As a merchant, I want to have a cleaner shop info page without unnecessary sections, so that I can focus on essential shop information management.

#### Acceptance Criteria

1. WHEN the shop info page loads THEN the Shop_Info_Page SHALL NOT display the shop album section (店铺图片/店铺相册)
2. WHEN the shop info page loads THEN the Shop_Info_Page SHALL NOT display the debug information section (调试信息)
3. WHEN the shop info page renders THEN the Shop_Info_Page SHALL maintain the logo upload functionality in the basic info section
4. WHEN the shop info page renders THEN the Shop_Info_Page SHALL maintain all other existing functionality including basic info, contact info, address, and operation data sections

### Requirement 2

**User Story:** As a merchant, I want to have an edit button on the shop info page, so that I can easily modify my shop information.

#### Acceptance Criteria

1. WHEN the shop info page loads THEN the Shop_Info_Page SHALL display an Edit_Button in the page header area (right side of the title)
2. WHEN a merchant clicks the Edit_Button THEN the Shop_Info_Page SHALL enable editing mode for all editable fields
3. WHEN editing mode is active THEN the Shop_Info_Page SHALL display save and cancel buttons at the bottom of the page

### Requirement 3

**User Story:** As a merchant, I want to input address with auto-fill support for province/city/district, so that I can quickly and accurately enter my shop location.

#### Acceptance Criteria

1. WHEN a merchant enters a detailed address THEN the Address_Autocomplete SHALL parse and auto-fill the province, city, and district fields
2. WHEN the province/city/district fields are auto-filled THEN the Shop_Info_Page SHALL allow the merchant to manually edit these fields
3. WHEN a merchant manually inputs province/city/district THEN the Shop_Info_Page SHALL accept the manual input and update the form accordingly
4. WHEN the address input changes THEN the Geocoding service SHALL calculate latitude and longitude based on the complete address
5. WHEN geocoding completes THEN the Shop_Info_Page SHALL auto-fill the latitude and longitude fields with the calculated coordinates

### Requirement 4

**User Story:** As a user, I want to be redirected to the login page when accessing the root URL without authentication, so that I can properly authenticate before using the merchant center.

#### Acceptance Criteria

1. WHEN an unauthenticated user accesses http://localhost:3000/ THEN the Login_Redirect SHALL navigate the user to the login page
2. WHEN an authenticated user accesses http://localhost:3000/ THEN the Login_Redirect SHALL navigate the user to the shop management page
3. WHEN the login page loads THEN the Shop_Info_Page SHALL display the login form correctly without any errors
