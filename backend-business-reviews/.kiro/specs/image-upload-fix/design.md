# Image Upload Fix Design Document

## Overview

This design addresses the image upload functionality issues in the merchant management system. The current implementation fails to properly upload images to Aliyun OSS due to incorrect request configuration and error handling. This design provides a comprehensive solution to fix the upload mechanism and ensure proper integration with both the merchant interface and UniApp client.

## Architecture

### Current Issues Identified

1. **Request Configuration**: The current upload function uses fetch API with manual header configuration, which may not properly handle FormData boundaries
2. **Error Handling**: Limited error handling and debugging information
3. **Authentication**: Potential issues with token-based authentication for file uploads
4. **Response Parsing**: Inconsistent handling of OSS service responses

### Proposed Solution

1. **Unified Upload Service**: Create a dedicated upload service using the existing axios instance
2. **Proper FormData Handling**: Ensure correct Content-Type and boundary handling
3. **Enhanced Error Handling**: Comprehensive error catching and user feedback
4. **Debug Logging**: Detailed logging for troubleshooting

## Components and Interfaces

### Upload Service Interface

```typescript
interface UploadService {
  uploadSingleFile(file: File, folder?: string): Promise<string>
  uploadMultipleFiles(files: File[], folder?: string): Promise<string[]>
}
```

### Upload Response Interface

```typescript
interface UploadResponse {
  code: number
  data: {
    url: string
  }
  msg?: string
}
```

## Data Models

### File Upload Request

```typescript
interface FileUploadRequest {
  file: File
  folder: string
  token: string
}
```

### Upload Result

```typescript
interface UploadResult {
  success: boolean
  url?: string
  error?: string
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Upload Request Format
*For any* valid image file, the upload request should include proper FormData with file and folder parameters
**Validates: Requirements 4.1, 4.3**

### Property 2: Authentication Token Inclusion
*For any* upload request, the system should include the merchant authentication token in the Authorization header
**Validates: Requirements 4.2**

### Property 3: Response URL Validation
*For any* successful upload response, the returned URL should be a valid OSS URL that can be accessed
**Validates: Requirements 4.4**

### Property 4: Error Message Clarity
*For any* upload failure, the system should provide a clear, actionable error message to the user
**Validates: Requirements 3.1, 3.2, 3.3, 3.4**

### Property 5: File Validation Consistency
*For any* file upload attempt, files exceeding size limits or invalid formats should be rejected before upload
**Validates: Requirements 1.5**

### Property 6: Gallery State Consistency
*For any* gallery operation (add/remove), the gallery state should remain consistent with the backend data
**Validates: Requirements 2.3, 2.4**

## Error Handling

### Upload Error Categories

1. **Validation Errors**: File size, format, count limits
2. **Network Errors**: Connection issues, timeouts
3. **Authentication Errors**: Invalid or expired tokens
4. **Service Errors**: OSS service unavailable, quota exceeded
5. **Response Errors**: Invalid response format, missing URL

### Error Recovery Strategies

1. **Retry Logic**: Automatic retry for network-related failures
2. **Fallback Messaging**: Clear user instructions for recoverable errors
3. **Logging**: Comprehensive error logging for debugging
4. **State Cleanup**: Proper cleanup of partial upload states

## Testing Strategy

### Unit Testing

- File validation logic
- Request formatting
- Response parsing
- Error handling paths

### Integration Testing

- End-to-end upload flow
- Authentication integration
- OSS service integration
- Error scenario testing

### Property-Based Testing

Using **fast-check** library for JavaScript/TypeScript:

- Generate random valid/invalid files for upload testing
- Test upload request formatting across various file types
- Verify error handling with different failure scenarios
- Test gallery state consistency with random operations

Each property-based test should run a minimum of 100 iterations to ensure comprehensive coverage.

## Implementation Plan

### Phase 1: Core Upload Service
1. Create dedicated upload service module
2. Implement proper FormData handling
3. Add comprehensive error handling
4. Integrate with existing axios instance

### Phase 2: UI Integration
1. Update shop cover upload component
2. Update gallery upload component
3. Enhance user feedback and loading states
4. Add proper error display

### Phase 3: Testing and Validation
1. Implement unit tests for upload service
2. Add property-based tests for upload scenarios
3. Test integration with OSS service
4. Validate UniApp client display

### Phase 4: Monitoring and Logging
1. Add detailed upload logging
2. Implement upload analytics
3. Add performance monitoring
4. Create error reporting dashboard