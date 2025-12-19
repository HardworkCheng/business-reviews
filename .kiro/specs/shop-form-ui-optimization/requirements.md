# Requirements Document

## Introduction

本功能旨在优化商家运营中心的店铺信息管理页面表单体验，主要包括：修复输入框无法输入的问题、将省份/城市/区县三个独立输入框合并为一行统一的地址输入框、在地址输入框右侧显示经纬度和定位按钮、将店铺简介缩小为一半宽度并在右侧添加商家相册上传功能，商家相册需要在 UniApp 移动端商家详情页面中展示。

## Glossary

- **Shop_Info_Page**: 店铺信息管理页面，商家用于管理店铺基本信息的界面
- **Address_Input**: 地址输入组件，用于输入和显示店铺详细地址
- **Location_Button**: 定位按钮，用于获取当前位置的经纬度坐标
- **Coordinates_Display**: 经纬度显示区域，显示店铺位置的经度和纬度
- **Shop_Gallery**: 商家相册，用于上传和展示店铺的多张图片
- **Mobile_Shop_Detail**: UniApp 移动端商家详情页面，展示店铺信息和相册

## Requirements

### Requirement 1

**User Story:** As a merchant, I want all input fields to be editable when in edit mode, so that I can modify my shop information without any restrictions.

#### Acceptance Criteria

1. WHEN the merchant clicks the edit button THEN the Shop_Info_Page SHALL enable all input fields for editing
2. WHEN the edit mode is active THEN the Shop_Info_Page SHALL allow text input in all form fields including name, description, phone, and address
3. WHEN the edit mode is inactive THEN the Shop_Info_Page SHALL disable all input fields to prevent accidental modifications

### Requirement 2

**User Story:** As a merchant, I want a simplified address input with province/city/district combined into one line, so that I can view and edit my shop location more efficiently.

#### Acceptance Criteria

1. WHEN the Shop_Info_Page renders the address section THEN the Address_Input SHALL display province, city, and district in a single row layout
2. WHEN the merchant edits the address THEN the Address_Input SHALL allow manual input for province, city, and district fields in the combined row
3. WHEN the address is auto-filled from geocoding THEN the Address_Input SHALL update all three fields (province, city, district) in the single row layout

### Requirement 3

**User Story:** As a merchant, I want to see coordinates and have a location button next to the address input, so that I can easily locate my shop and verify the coordinates.

#### Acceptance Criteria

1. WHEN the Shop_Info_Page renders the address section THEN the Coordinates_Display SHALL appear on the right side of the address input row
2. WHEN the Shop_Info_Page renders THEN the Location_Button SHALL appear next to the coordinates display
3. WHEN the merchant clicks the Location_Button THEN the Shop_Info_Page SHALL obtain the current GPS coordinates and update the latitude and longitude fields
4. WHEN coordinates are obtained THEN the Coordinates_Display SHALL show the latitude and longitude values in a readable format

### Requirement 4

**User Story:** As a merchant, I want the shop description field to be smaller and have a gallery section next to it, so that I can add photos to showcase my shop.

#### Acceptance Criteria

1. WHEN the Shop_Info_Page renders the basic info section THEN the description field SHALL occupy half of the available width
2. WHEN the Shop_Info_Page renders THEN the Shop_Gallery SHALL appear on the right side of the description field
3. WHEN the merchant uploads images to the Shop_Gallery THEN the Shop_Info_Page SHALL store the image URLs in the shop data
4. WHEN the merchant removes an image from the Shop_Gallery THEN the Shop_Info_Page SHALL update the shop data to remove the image URL
5. WHEN the Shop_Gallery contains images THEN the Shop_Info_Page SHALL display thumbnail previews of all uploaded images

### Requirement 5

**User Story:** As a mobile user, I want to see the shop gallery on the shop detail page, so that I can view photos of the shop before visiting.

#### Acceptance Criteria

1. WHEN the Mobile_Shop_Detail page loads THEN the page SHALL display the Shop_Gallery images from the shop data
2. WHEN the shop has multiple gallery images THEN the Mobile_Shop_Detail SHALL display the images in a scrollable or grid layout
3. WHEN the user taps on a gallery image THEN the Mobile_Shop_Detail SHALL display the image in a full-screen preview mode
