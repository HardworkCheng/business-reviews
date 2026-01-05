# Image Upload Fix Requirements

## Introduction

Fix the image upload functionality in the merchant management system to properly upload images to Aliyun OSS and display them correctly in both the merchant center and the UniApp user interface.

## Glossary

- **Merchant_System**: The web-based merchant management interface
- **OSS_Service**: Aliyun Object Storage Service for file storage
- **UniApp_Client**: The mobile application for end users
- **Upload_Endpoint**: The backend API endpoint `/api/merchant/upload`

## Requirements

### Requirement 1

**User Story:** As a merchant, I want to upload a shop cover image, so that customers can see my shop's visual representation.

#### Acceptance Criteria

1. WHEN a merchant clicks the cover upload area in edit mode, THEN the Merchant_System SHALL open a file selection dialog
2. WHEN a merchant selects a valid image file (JPG, PNG, GIF, WEBP, BMP), THEN the Merchant_System SHALL upload the file to OSS_Service
3. WHEN the upload is successful, THEN the Merchant_System SHALL display the uploaded image immediately
4. WHEN the shop data is saved, THEN the UniApp_Client SHALL display the updated cover image
5. IF the file size exceeds 10MB, THEN the Merchant_System SHALL prevent upload and show an error message

### Requirement 2

**User Story:** As a merchant, I want to upload multiple gallery images, so that customers can see various aspects of my shop.

#### Acceptance Criteria

1. WHEN a merchant clicks the gallery upload area in edit mode, THEN the Merchant_System SHALL open a multi-file selection dialog
2. WHEN a merchant selects up to 9 valid image files, THEN the Merchant_System SHALL upload each file to OSS_Service
3. WHEN uploads are successful, THEN the Merchant_System SHALL display all uploaded images in the gallery grid
4. WHEN a merchant clicks the delete button on a gallery image, THEN the Merchant_System SHALL remove the image from the gallery
5. WHEN the shop data is saved, THEN the UniApp_Client SHALL display all gallery images

### Requirement 3

**User Story:** As a system administrator, I want proper error handling for upload failures, so that merchants receive clear feedback.

#### Acceptance Criteria

1. WHEN an upload request fails due to network issues, THEN the Merchant_System SHALL display a network error message
2. WHEN an upload request fails due to authentication, THEN the Merchant_System SHALL redirect to login page
3. WHEN an upload request fails due to file validation, THEN the Merchant_System SHALL display the specific validation error
4. WHEN an upload request fails due to OSS_Service issues, THEN the Merchant_System SHALL display a service error message
5. WHEN any upload error occurs, THEN the Merchant_System SHALL log detailed error information for debugging

### Requirement 4

**User Story:** As a developer, I want proper request configuration for file uploads, so that the upload functionality works reliably.

#### Acceptance Criteria

1. WHEN making file upload requests, THEN the Merchant_System SHALL use FormData with proper Content-Type headers
2. WHEN uploading files, THEN the Merchant_System SHALL include the merchant authentication token
3. WHEN uploading files, THEN the Merchant_System SHALL include the folder parameter for OSS organization
4. WHEN receiving upload responses, THEN the Merchant_System SHALL properly parse the OSS URL from the response
5. WHEN upload requests timeout, THEN the Merchant_System SHALL handle the timeout gracefully