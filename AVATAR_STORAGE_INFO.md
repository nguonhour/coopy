# Avatar Image Storage - Important Information

## Current Implementation

Profile avatars are stored locally in the `uploads/avatars/` directory on your computer.

## Issues You May Experience

### 1. **Images don't appear on other computers**

When you upload a profile picture on your computer:

- The image is saved to `uploads/avatars/` in your local project directory
- This directory is **NOT** committed to Git (it's in `.gitignore`)
- When your friend clones or pulls the repository, they won't get your uploaded images
- Each person will only see images they uploaded on their own computer

### 2. **Users showing same avatar (FIXED)**

Previously, all users displayed the same avatar because the JavaScript was loading `/api/users/me/avatar-image` which always returned the **currently logged-in user's** image, not the profile owner's image.

**This has been fixed:** The avatar URL now includes `?userId={id}` to show the correct user's avatar.

## Solutions

### Short-term (for development/testing)

**Option 1: Share the uploads folder manually**

1. Zip the `uploads/avatars/` folder
2. Share it with your team
3. They extract it to the same location in their project
4. **Limitation:** Images must be reshared every time someone uploads new ones

**Option 2: Commit uploads folder (NOT recommended for production)**

1. Remove `uploads/` from `.gitignore`
2. Commit the uploads folder to Git
3. Everyone gets the same images
4. **Warning:** This will make your repository larger and is not secure for production

### Long-term (recommended for production)

**Use Cloud Storage:**

1. **AWS S3** (Amazon)
2. **Azure Blob Storage** (Microsoft)
3. **Google Cloud Storage**
4. **Cloudinary** (specialized for images)

**Benefits:**

- All users see the same images
- Images are backed up
- Better performance with CDN
- Scalable for production

### Configuration for Cloud Storage

To migrate to cloud storage, you would need to:

1. Update `UserService.java`:

   - Change `updateAvatar()` to upload to cloud instead of local disk
   - Change `loadAvatarResource()` to return cloud URL or redirect

2. Add cloud SDK dependency to `pom.xml`:

```xml
<!-- Example for AWS S3 -->
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-java-sdk-s3</artifactId>
    <version>1.12.x</version>
</dependency>
```

3. Update `application.properties`:

```properties
# AWS S3 example
cloud.aws.credentials.access-key=YOUR_ACCESS_KEY
cloud.aws.credentials.secret-key=YOUR_SECRET_KEY
cloud.aws.region.static=us-east-1
cloud.aws.s3.bucket=your-bucket-name
```

## Current Fix Applied

✅ **Avatar URL now includes userId parameter:**

- Before: `/api/users/me/avatar-image` (always showed logged-in user's image)
- After: `/api/users/me/avatar-image?userId={userId}` (shows specific user's image)

✅ **New public endpoint added:**

- `/api/users/avatar/{userId}` - Public access to any user's avatar

✅ **Security updated:**

- Avatar endpoints are now publicly accessible (read-only)
- Upload still requires authentication

## Testing the Fix

1. Login as admin
2. Upload an avatar
3. Logout and login as lecturer
4. Upload a different avatar
5. Go to lecturer profile → should see lecturer's image
6. Go to admin profile (if you have permission) → should see admin's image
7. Each user now sees their own correct avatar!

**Note:** On your friend's computer, they will see the default avatar SVG until they upload their own images or you share the uploads folder.
