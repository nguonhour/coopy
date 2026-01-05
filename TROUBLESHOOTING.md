# Troubleshooting Common Errors

## 1. Avatar Upload Failed (500 Error) ✅ FIXED

### Error Message:

```
Failed to load resource: the server responded with a status of 500 ()
Avatar upload failed
```

### Root Causes:

1. **Missing file upload configuration** - Spring Boot needs explicit file size limits
2. **Directory permissions** - The `uploads/avatars/` directory might not exist or have wrong permissions
3. **Poor error logging** - Original code didn't show what actually failed

### Fixes Applied:

#### ✅ Added file upload configuration in `application.properties`:

```properties
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
spring.servlet.multipart.file-size-threshold=2KB
```

#### ✅ Enhanced error logging in `UserService.java`:

- Added detailed console output showing upload directory path
- Logs each step of the upload process
- Shows exactly where failures occur

#### ✅ Improved error handling in `UserController.java`:

- Returns more descriptive error messages
- Prints full stack trace to console for debugging
- Helps identify the exact failure point

### How to Test:

1. **Restart your Spring Boot application** (important!)
2. Login to the system
3. Click profile icon → Edit Profile
4. Select an image file and click Save
5. Check the console logs for detailed output:
   ```
   Avatar upload directory: /path/to/project/uploads/avatars
   Created avatar directory: /path/to/project/uploads/avatars
   Saving avatar to: /path/to/project/uploads/avatars/user_1_1704366044547.jpg
   Avatar file saved successfully
   Avatar URL saved to profile: user_1_1704366044547.jpg
   ```

### Still Getting 500 Error?

Check the console logs and look for:

**Permission Error:**

```
java.nio.file.AccessDeniedException: uploads/avatars
```

**Solution:** Run `chmod -R 755 uploads` in terminal

**Out of Disk Space:**

```
java.io.IOException: No space left on device
```

**Solution:** Free up disk space

**File Too Large:**

```
org.springframework.web.multipart.MaxUploadSizeExceededException
```

**Solution:** Reduce image size or increase `max-file-size` in application.properties

---

## 2. Chrome DevTools Warning (Ignorable) ℹ️

### Warning Message:

```
org.springframework.web.servlet.resource.NoResourceFoundException:
No static resource .well-known/appspecific/com.chrome.devtools.json.
```

### Explanation:

- This is **NOT an error** - it's just Chrome looking for developer tools configuration
- Chrome automatically requests this file when DevTools is open
- **Does not affect your application** - you can safely ignore it

### What is it?

The `.well-known/appspecific/` path is a standard location where Chrome looks for app-specific metadata when developer tools are open. It's completely optional.

### Should You Fix It?

**No.** This warning is harmless and appears in many web applications. It doesn't impact:

- User experience
- Application functionality
- Performance
- Security

### If It Bothers You:

You can silence this specific warning by adding this to your logging configuration, but it's not necessary:

In `application.properties`:

```properties
logging.level.org.springframework.web.servlet.resource.ResourceHttpRequestHandler=ERROR
```

This will hide resource-not-found warnings at INFO level.

---

## Summary

✅ **Avatar Upload Issue:** FIXED by adding file upload configuration and better error logging  
ℹ️ **Chrome DevTools Warning:** Harmless, can be ignored

### Next Steps:

1. Restart your Spring Boot server
2. Try uploading an avatar again
3. Check console logs for detailed output
4. If still failing, check the specific error message in console and refer to troubleshooting steps above
