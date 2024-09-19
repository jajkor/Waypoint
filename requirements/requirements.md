# Team Info & Policies

## Team Members & Roles

- **Adam Jeffery Zorgo**  
  UI/UX Designer
- **Robert Jajko**  
  Graphics Programmer
- **Sarim Kazmi**  
  Database Programmer
- **Thomas Patrick McLinden**  
  Pathfinding Programmer

## Project Artifacts

- **Git Repository**: [https://github.com/jajkor/Waypoint](https://github.com/jajkor/Waypoint)

## Communication Channels

- **Discord** for daily communication and updates
- **GitHub issues** for task assignments and tracking progress

## Project Description

**Waypoint** is an interactive 3D navigation app for Android devices that aims to help students, faculty, and visitors of Penn State Abington better navigate the campus.

## Major Features

- Interactive 3D map of the campus
- Real-time location tracking
- Building room database
- Room search functionality
- Pathfinding to specific rooms or areas of interest

### Stretch Goals

- Implement Sutherland building

## Use Cases

### Use Case 1: Campus Navigation

- **Actors**: Student
- **Triggers**: A student needs to find a classroom in a new building.
- **Preconditions**: The app is installed, GPS is allowed and active.
- **Postconditions**: User receives accurate and readable directions and successfully reaches the classroom.

#### List of steps:

1. User opens the app and inputs their destination.
2. The app calculates the optimal path from their current location.
3. The app provides turn-by-turn navigation.
4. User reaches their destination and the app confirms arrival.

- **Exceptions**: GPS signal loss, users using mobile data may be affected indoors.

### Use Case 2: Object of Interest Navigation

- **Actors**: Faculty
- **Triggers**: A faculty member needs to find a PawPrint printer in the building.
- **Preconditions**: The app is installed, GPS is allowed and active.
- **Postconditions**: User receives accurate and readable directions and successfully reaches the printer.

#### List of steps:

1. User opens the app and inputs their destination.
2. The app calculates the optimal path from their current location.
3. The app provides turn-by-turn navigation.
4. User reaches their destination and the app confirms arrival.

- **Exceptions**: GPS signal loss, users using mobile data may be affected indoors.

### Use Case 3: Campus Navigation

- **Actors**: Visitor
- **Triggers**: A visitor wants to find the parking office to get their parking permit before school starts.
- **Preconditions**: The app is installed, GPS is allowed and active.
- **Postconditions**: User receives accurate and readable directions and successfully reaches the parking office.

#### List of steps:

1. User opens the app and inputs their destination.
2. The app calculates the optimal path from their current location.
3. The app provides turn-by-turn navigation.
4. User reaches their destination and the app confirms arrival.

- **Exceptions**: GPS signal loss, users using mobile data may be affected indoors. Navigation may be difficult between buildings.

## Non-functional Requirements

### Usability

- UI must require minimal learning for first-time users.
- Google Maps users can intuitively use Waypoint.

### Performance

- The interactive map and directions should load within 1 second to ensure a smooth user experience.

### Readability

- Users must be able to easily identify where they are and distinguish where their path is.

> If users can’t quickly learn to use the app, the app takes too long to be useful, and the information is difficult to see. The users will very quickly ditch the app and go to external assistance.

## External Requirements

### Installability

- The app should be quick and easy to download, install, and use via the Google Play Store.
- Download size should be kept as low as possible.

### Robustness

- The app must handle expected error scenarios like poor GPS signal or invalid searches, providing clear feedback to the user.

### Source Code

- All code must be documented and buildable from source, with clear instructions for setting up the development environment.

## Team Process Description

### Toolset

- **Kotlin**  
  Language chosen for Android app development.  
  Google announced Android development is going to be increasingly Kotlin-first.  
  Safer and more concise than Java.  
  Interoperability with Java.
  
- **OpenGL ES 3.0**  
  Subset of the OpenGL API for embedded systems such as phones.  
  Chosen for rendering 3D models of the campus.  
  Supported by devices using Android 4.3 and higher.  
  Simpler than the Vulkan Graphics API.

- **SQLite**  
  Local storage of building information.  
  Offline database stored on the user’s device.  
  Storing room information, a finite amount of data.

- **Git & GitHub**  
  Version control for collaborative development.  
  Widely used in industry.  
  Enables working on a single project as a group.

- **Discord**  
  Daily communication and updates.  
  Free and easy to use.

### Roles

- **UI/UX Designer**  
  Designs the UI elements and ensures the UI is intuitive and simple.

- **Graphics Programmer**  
  Handles the rendering using OpenGL ES.

- **Database Programmer**  
  Manages the SQLite database and backend logic.

- **Pathfinding Programmer**  
  Handles the logic for the navigation.

## Milestones

- Initial 3D model of the campus completed.
- 3D model rendering and shading completed.
- User interaction via touch gestures completed.
- Room database completed.
- Search functionality completed.
- Basic navigation functionality completed.
- Testing, bug fixes, and stretch goals.

## Risk

- GPS Inaccuracy
- Team Coordination

## External Feedback

- Feedback will be solicited from faculty and students after the MVP (minimum viable product) is functional, targeting usability and feature effectiveness.
