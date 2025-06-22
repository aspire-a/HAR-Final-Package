# HARProjecApp

## Installation & Setup

1. Prerequisites  
   - Android Studio 4.2 or newer  
   - Java JDK 8 or higher  

2. Open in Android Studio  
   - Launch Android Studio  
   - Choose “Open an existing project” and select the 'HarProjecApp' folder  

3. Sync Gradle  
   - Click “Sync Now” when prompted to download all dependencies  

4. Configure API endpoint  
   - In the project root, create or update `local.properties`  
   - Add your backend URL:  
     ```properties
     HAR_API_BASE_URL="https://your.api.endpoint.com/"
     ```

5. Prepare device or emulator  
   - Start an Android Virtual Device (AVD) via AVD Manager  
     or  
   - Connect a physical device with USB debugging enabled  

## Running the App

1. Select your target device or emulator in Android Studio  
2. Click Run (or press Shift + F10)  
3. Wait for the build to complete and the app to launch  

---