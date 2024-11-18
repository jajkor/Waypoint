# Waypoint - 3D Indoor Navigation for Android

![Build Status](https://img.shields.io/github/actions/workflow/status/jajkor/Waypoint/main.yml) ![License](https://img.shields.io/github/license/jajkor/Waypoint) ![Min API Level](https://img.shields.io/badge/min--api-24-brightgreen) ![OpenGL ES 3.2](https://img.shields.io/badge/OpenGL--ES-3.2-blue) ![Last Commit](https://img.shields.io/github/last-commit/jajkor/Waypoint) ![GitHub issues](https://img.shields.io/github/issues/jajkor/Waypoint)

## Overview

**Waypoint** is a 3D indoor navigation application designed to help students, visitors, and faculty of Penn State Abington navigate the campus with ease. The app features a 3D representation of the campus, real-time pathfinding, and intuitive guidance to important locations such as classrooms, offices, and common areas.

- **3D Campus Visualization**: View an accurate, interactive 3D model of the Penn State Abington campus.
- **Indoor Location Tracking**: Uses Wi-Fi RSSI triangulation to enhance indoor navigation accuracy.
- **Interactive Map**: Enables users to interact with the map through swipe, pinch, and zoom gestures.


## Permissions Required

For the Waypoint app to function optimally, it requires several permissions to access Wi-Fi and location data. These permissions enable the app to determine your location accurately within indoor environments using Wi-Fi scanning. When you launch the app for the first time, you will be prompted to grant these permissions.

#### Permissions and Their Purposes

1. **Wi-Fi Permissions**:
   - **Purpose**: These permissions allow the app to scan for Wi-Fi networks and use signal strength (RSSI values) to improve indoor location accuracy.
   - **Permissions Required**:
     - `ACCESS_WIFI_STATE`: Allows the app to view Wi-Fi connections.
     - `CHANGE_WIFI_STATE`: Enables the app to initiate Wi-Fi scans.
     - `NEARBY_WIFI_DEVICES`: Provides access to nearby Wi-Fi devices for enhanced location tracking.

2. **Location Permissions**:
   - **Purpose**: Necessary for retrieving accurate location data while navigating indoor spaces.
   - **Permissions Required**:
     - `ACCESS_FINE_LOCATION`: Enables high-accuracy location data using both GPS and network location.
     - `ACCESS_COARSE_LOCATION`: Allows approximate location data, which is helpful for Wi-Fi-based triangulation.

#### Granting Permissions

When the app launches for the first time:
   1. A permission prompt will appear requesting access to Wi-Fi and location data.
   2. Select **Allow** for each permission to enable full functionality of the Waypoint app.
   3. If permissions are not granted initially, you can enable them manually:
      - **Go to**: **Settings** > **Apps** > **Waypoint** > **Permissions**.
      - Enable **Wi-Fi** and **Location** permissions.

Ensuring that these permissions are granted will allow the Waypoint app to access Wi-Fi scan results and location data, enhancing its indoor navigation accuracy.


## Build Instructions

To set up and run the Waypoint app, follow these steps:

1. **Clone the Repository**:
   - Open a terminal or command prompt.
   - Run the following command to clone the Waypoint repository:
     ```bash
     git clone https://github.com/jajkor/Waypoint.git
     ```

2. **Open the Project in Android Studio**:
   - Launch **Android Studio**.
   - Select **File** > **Open** and navigate to the location where you cloned the repository.
   - Choose the `Waypoint` folder and click **OK** to open the project.

3. **Sync Gradle and Install Dependencies**:
   - Once the project loads, Android Studio will prompt you to sync Gradle.
   - Click **Sync Now** to ensure all dependencies are downloaded and configured properly.

4. **Connect an Android Device or Launch an Emulator**:
   - **To use a physical device**: Connect your Android device to your computer via USB and enable **Developer Options** and **USB Debugging**.
   - **To use an emulator**: In Android Studio, go to **Device Manager** and start a virtual device (emulator) that meets the app’s requirements (Android 7.0, OpenGL ES 3.2 support).

5. **Build and Run the Project**:
   - In Android Studio, select **Run** > **Run 'app'** or click the green **Play** button in the toolbar.
   - Android Studio will build the project and install the app on your connected device or emulator.

After following these steps, the Waypoint app should be up and running, allowing you to test its features and functionality.


## Setup Guide
### Disable Wi-Fi Scan Throttling

For the Waypoint app to function correctly, it is necessary to disable Wi-Fi Scan Throttling in the developer options. This setting allows the app to perform frequent Wi-Fi scans, which is essential for accurate indoor location tracking.

#### Steps to Disable Wi-Fi Scan Throttling:

1. **Enable Developer Options (if not already enabled)**:
   - Go to **Settings** > **About Phone**.
   - Scroll down to **Build Number** and tap it **seven times** until you see a message that says "You are now a developer!"
   - Enter your device PIN if prompted.

2. **Access Developer Options**:
   - Go back to **Settings**.
   - Scroll down to **System** (or **Additional settings** on some devices) and tap **Developer options**.

3. **Disable Wi-Fi Scan Throttling**:
   - In the Developer options menu, scroll down to the **Networking** section.
   - Find **Wi-Fi scan throttling** and toggle it **off**.

4. **Verify Setting**:
   - Ensure that **Wi-Fi scan throttling** remains disabled to allow the Waypoint app to update location information frequently.

After completing these steps, Wi-Fi scanning will no longer be throttled, enabling Waypoint to scan for Wi-Fi access points as needed for precise indoor positioning.

---

### Importing 3D Models

To render the campus buildings accurately, you need to import the necessary 3D model files into the project.

#### Steps to Import 3D Models:

1. **Locate or Create the 3D Model Files**:
   - Ensure your 3D model files are in the `.obj` format, which is compatible with the application.
   - These models should accurately represent the campus buildings for proper rendering.

2. **Add 3D Models to the Assets Folder**:
   - Place all `.obj` files in the app's `assets` folder within the project directory. This will make them accessible to the app's rendering engine.
   - The folder path should resemble: `app/src/main/assets/`.

3. **Modify the `ModelLoader` Class (if necessary)**:
   - If your models use custom formats, specific textures, or shading, update the `ModelLoader` class to handle these requirements.
   - This may involve adjusting how the loader reads and interprets vertex data, textures, or shading settings.

4. **Verify Import in Android Studio**:
   - Open Android Studio, sync your project, and confirm that the models load correctly within the app.
   - Run the app to check if the models render as expected on the 3D map.

---

### Wi-Fi Access Point Configuration

Since the Waypoint app uses Wi-Fi RSSI triangulation for location accuracy, it's important to ensure a suitable Wi-Fi environment.

#### Steps to Configure Wi-Fi Access Points:

1. **Access to Minimum Wi-Fi Points**:
   - Ensure that at least three Wi-Fi access points are within the navigation area to allow for triangulation.
   - These access points help the app determine the user’s location more accurately.

2. **Optimize Placement of Access Points**:
   - For best results, position access points in a way that minimizes interference and provides consistent coverage across the navigation area.
   - Spread access points evenly if possible, covering critical areas where navigation is needed.

3. **Verify Connectivity**:
   - Test the app in the area to confirm that it can detect the [Wi-Fi access points](https://play.google.com/store/apps/details?id=com.google.android.apps.location.rtt.wifirttscan&hl=en_US).
   - Ensure that the RSSI values from these points are stable and sufficient for accurate triangulation.

## Usage Guide
### 3D Map Interactions

The Waypoint app provides intuitive gestures to help users interact with the 3D map, allowing them to explore the campus from various perspectives.

#### Steps for 3D Map Interactions:

1. **Rotate the View**:
   - Swipe left or right on the screen to rotate the 3D map.
   - This gesture allows you to view different angles of the campus, making it easier to navigate specific areas.

2. **Zoom In/Out**:
   - Use a pinch gesture to zoom in and out of the 3D map.
   - Pinch inward with two fingers to zoom out for a broader view of the campus.
   - Pinch outward to zoom in and focus on specific areas or details within buildings.

By mastering these gestures, you can easily explore and interact with the 3D campus map, enhancing your navigation experience within the Waypoint app.

### Testing

To ensure the Waypoint app performs reliably, follow these testing steps, which cover unit tests, UI functionality, device compatibility, and real-world conditions.

#### 1. Unit Tests
   - **Purpose**: Verify the correctness of core functions such as navigation algorithms and database interactions.
   - **Implementation**: Write unit tests to cover the main functionalities, ensuring they behave as expected under various conditions.
   - **Execution**:
     - Run tests using Android Studio’s built-in testing tools.
     - Alternatively, execute tests from the command line with:
       ```bash
       ./gradlew testDebugUnitTest
       ```

#### 2. UI Testing
   - **Purpose**: Confirm that all user interface elements work as intended and handle various interactions gracefully.
   - **Implementation**: 
     - Use Android’s **UI Automator** for structured UI testing to ensure gestures and navigation features work as expected.
     - Run a **Monkey test** to simulate random user interactions, which helps identify any potential crashes or UI issues due to unexpected inputs.
   - **Execution**:
     - **UI Automator**: Test gestures like pinch-to-zoom, swiping, and button taps to ensure smooth operation.
     - **Monkey Test**: Run the following command to perform a Monkey test with 1,000 random events:
       ```bash
       adb shell monkey -p com.example.waypoint -v 1000
       ```
       - Replace `com.example.waypoint` with your actual package name.
       - Adjust the number of events (`1000` in this example) as needed to cover more interactions.

By including the Monkey test, you ensure the app can handle unexpected user interactions, enhancing its robustness and stability in real-world use.

#### 3. Real-World Testing
   - **Purpose**: Validate app performance in real-world scenarios within campus buildings, ensuring accurate navigation.
   - **Implementation**: Use the app in multiple buildings across the campus.
   - **Verification**:
     - Run Wi-Fi-based location tests in areas with different numbers of access points.
     - Evaluate the app’s triangulation accuracy across devices to verify consistent performance.
     - Note any location tracking discrepancies and, if needed, adjust the Wi-Fi triangulation parameters to improve accuracy.

By following these testing steps, you can verify that the Waypoint app meets quality standards, functions seamlessly across devices, and provides reliable navigation in real-world environments.


## License
This project is licensed under the MIT License - see the [License](https://github.com/javagl/Obj) file for details.


## Acknowledgments
- [JavaGL](https://github.com/javagl/Obj): For the `Obj` library, which assists in handling 3D model loading.
- Township of Abington: For providing resources to support the 3D campus map.
- Penn State Abington: For supporting the project and providing access to campus facilities for testing and development.
