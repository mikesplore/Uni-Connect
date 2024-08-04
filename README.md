## UNI ADMIN Documentation

### Table of Contents

- [Authentication](#authentication)
    - [Overview](#overview)
    - [Google Authentication](#google-authentication)
    - [GitHub Authentication](#github-authentication)
    - [Authentication Screen](#authentication-screen)
    - [MoreDetails Composable](#moreDetails-composable)
    - [Password Reset](#password-reset)
- [Visuals](#visuals)

## Authentication

### Overview
This section provides a detailed overview of the components and functionality related to user authentication within Uni Admin. It covers different authentication methods, including email/password login, third-party authentication using Google and GitHub, and password reset functionality. The documentation outlines the UI components, state management, and backend logic involved in each authentication process. It also includes references to relevant code files and visual representations of the user interface.

### Google Authentication

**File**: [GoogleAuth.kt](app/src/main/java/com/mike/uniadmin/authentication/GoogleAuth.kt)

**Functionality**

The `GoogleAuth` composable function handles Google authentication using [Firebase](https://firebase.google.com/docs/auth). It initiates the Google sign-in process and provides visual feedback to the user.

**Parameters**

- `firebaseAuth`: An instance of `FirebaseAuth` used to initiate the Google sign-in process.
- `onSignInSuccess`: A callback function triggered when the sign-in process is successful.
- `onSignInFailure`: A callback function triggered when the sign-in process fails, passing an error message.

**Behavior**

1. When the box is clicked, the function starts the Google sign-in process using Firebase Authentication.
2. While the sign-in process is ongoing, a circular progress indicator is displayed.
3. If the sign-in is successful, a check icon is displayed and `onSignInSuccess` is called.
4. If the sign-in fails, an error message is displayed and `onSignInFailure` is called.

### GitHub Authentication

**File**: [GithubAuth.kt](app/src/main/java/com/mike/uniadmin/authentication/GithubAuth.kt)

**Functionality**

The `GitAuth` composable function handles GitHub authentication using [Firebase](https://firebase.google.com/docs/auth). It initiates the GitHub sign-in process and provides visual feedback to the user.

**Parameters**

- `firebaseAuth`: An instance of `FirebaseAuth` used to initiate the GitHub sign-in process.
- `onSignInSuccess`: A callback function triggered when the sign-in process is successful.
- `onSignInFailure`: A callback function triggered when the sign-in process fails, passing an error message.

**Behavior**

1. When the box is clicked, the function starts the GitHub sign-in process using Firebase Authentication.
2. While the sign-in process is ongoing, a circular progress indicator is displayed.
3. If the sign-in is successful, a check icon is displayed and `onSignInSuccess` is called.
4. If the sign-in fails, an error message is displayed and `onSignInFailure` is called.

## Authentication Screen

**File**: [Login.kt](app/src/main/java/com/mike/uniadmin/authentication/Login.kt)

### Initialization

- **Firebase Authentication Instance:**
    - `val auth: FirebaseAuth = FirebaseAuth.getInstance()`: Initializes a `FirebaseAuth` instance.
- **State Variables:**
    - `firstName`, `lastName`, `email`, `password`: Holds user input values.
    - `isSigningUp`: Boolean to toggle between Sign Up and Sign In modes.
    - `isGithubLoading`, `isGoogleLoading`: Boolean flags for loading states during GitHub and Google sign-in.
    - `visible`: Controls visibility, initially set to true.
    - `loading`: Manages the loading state during form submission.
- **Repositories and ViewModels:**
    - Retrieves [notificationRepository](app/src/main/java/com/mike/uniadmin/dataModel/notifications/NotificationRepository.kt) and [userRepository](app/src/main/java/com/mike/uniadmin/dataModel/users/UserRepository.kt) from the application context.
    - Initializes [NotificationViewModel](app/src/main/java/com/mike/uniadmin/dataModel/notifications/NotificationsViewModel.kt) and [UserViewModel](app/src/main/java/com/mike/uniadmin/dataModel/users/UserViewModel.kt) using the respective repositories.

### UI Components

- **Top Bar:**
    - An empty top bar with the primary color.
- **Main Column:**
    - A vertical arrangement with text fields for user input and buttons for authentication options.
    - Displays "Sign In" or "Sign Up" text based on the `isSigningUp` state.
- **Third-Party Sign-In:**
    - `GoogleAuth` and `GitAuth` components handle third-party authentication.
    - On success, `handleAuthSuccess` is called.
    - On failure, a Toast message is displayed.
- **Email and Password Authentication:**
    - Text fields for email and password inputs.
    - If `isSigningUp` is true, additional fields for first and last name are shown.
    - A button to trigger `handleSignUp` or `handleSignIn` based on the `isSigningUp` state.
- **Password Reset:**
    - A link to navigate to the password reset screen when not in sign-up mode.
- **Toggle Sign Up/Sign In:**
    - A button to switch between Sign Up and Sign In modes with animated content transitions.

### Authentication Handlers

- **`handleAuthSuccess`:**
    - Called after successful third-party authentication.
    - Fetches user by email and navigates to the appropriate screen (`homescreen` or `moreDetails`).
- **`handleSignUp`:**
    - Validates email and password fields.
    - Registers the user with Firebase Authentication.
    - Creates a new [UserEntity](app/src/main/java/com/mike/uniadmin/dataModel/users/UserEntity.kt) and writes it to the `UserViewModel`.
    - Generates a notification indicating the new user has joined.
    - Displays appropriate Toast messages based on success or failure.
- **`handleSignIn`:**
    - Validates email and password fields.
    - Signs in the user with Firebase Authentication.
    - Fetches user details and navigates to the home screen.
    - Fetches the Firebase Cloud Messaging token and writes it to the database.
    - Displays appropriate Toast messages based on success or failure.

### LaunchedEffect

- Fetches the current user's email and retrieves user details when the composable is first composed.

This structure ensures the screen dynamically handles both sign-in and sign-up processes, manages third-party authentication, and updates the user interface accordingly based on user interactions and authentication states.

## MoreDetails Composable

**File**: [MoreDetails.kt](app/src/main/java/com/mike/uniadmin/ui/moreDetails/MoreDetails.kt)

### Description

The `MoreDetails` composable function displays a form for users to enter additional details like their first and last names after signing in. It also allows saving these details to the local database and notifies other users about the new user joining.

### Parameters

- `context`: `Context`: The context of the calling component.
- `navController`: `NavController`: The navigation controller to handle navigation between different screens.

### Functionality

- **Initialization:**
    - Retrieves the `UserRepository` and `NotificationRepository` from the `UniAdmin` application context.
    - Initializes the `UserViewModel` and `NotificationViewModel` using the repositories.
- **State Management:**
    - Uses `remember` and `mutableStateOf` to manage state variables for loading status (`addloading`), first name, and last name.
    - Retrieves the currently logged-in user from Firebase Authentication.
- **UI Layout:**
    - Applies a vertical gradient background using `Brush.verticalGradient`.
    - Displays a `TopAppBar` with a back navigation button.
    - Shows a title "Details" at the top.
    - Contains two text fields for the first and last names.
    - Provides a button to save the details.
- **Saving Details:**
    - On button click, generates a user ID and creates a `UserEntity` with the entered details.
    - Saves the user entity to the database using `UserViewModel`.
    - Generates a notification ID and writes a welcome notification using `NotificationViewModel`.
    - Navigates to the home screen upon successful saving of details.

### Important Notes

- The function ensures that the `UserRepository` is not null and throws an exception if it is.
- Uses `LaunchedEffect` to find the user by email when the composable is first launched.
- Utilizes a `Scaffold` to provide a consistent layout structure with a top app bar and a content area.

## Password Reset

**File**: [PasswordReset.kt](app/src/main/java/com/mike/uniadmin/authentication/PasswordReset.kt)

### Description

The `PasswordReset` composable function provides a screen for users to reset their forgotten passwords. It allows users to enter their email address and sends a password reset email using Firebase Authentication.

### Parameters

- `navController`: `NavController`: The navigation controller to handle navigation between different screens.
- `context`: `Context`: The context of the calling component.

### Functionality

- **Initialization:**
    - Initializes state variables for email input, loading state, and visibility.
    - Gets an instance of `FirebaseAuth` for authentication operations.
    - Sets up a vertical gradient background using `Brush.verticalGradient`.
- **UI Layout:**
    - Uses `AnimatedVisibility` to animate the screen content.
    - Uses `Scaffold` for basic layout structure with a top app bar (though not used) and content area.
    - Displays a title "Password Reset" at the top.
    - Includes a text field for email input.
    - Provides a button to trigger the password reset email.
- **Password Reset:**
    - When the button is clicked, it validates the email input.
    - If the email is empty, it shows a Toast message.
    - If the email is valid, it sends a password reset email using `auth.sendPasswordResetEmail()`.
    - Displays appropriate Toast messages based on success or failure.
    - Navigates to the login screen upon successful email sending.
- **Loading State:**
    - Shows a `CircularProgressIndicator` on the button while the password reset email is being sent.
- **Visibility:**
    - Uses `LaunchedEffect` to control the visibility of the screen content with an animation.

### Important Notes

- The function uses `remember` and `mutableStateOf` to manage state variables.
- It handles loading states and provides visual feedback to the user.
- It uses Toast messages to inform the user about the status of the password reset process.

## Visuals

### Sign In Screen
![Sign In](Images/SignIn.png)

### Sign Up Screen
![Sign Up](Images/SignUp.png)

### Password Reset Screen
![Password Reset](Images/passwordReset.png)

***
## Announcements

### Overview

The `AnnouncementsScreen` Composable function is responsible for displaying a screen where users can view, add, and manage announcements. It utilizes a top app bar with buttons for adding new announcements and refreshing the list. The screen presents a list of announcements retrieved from a repository and offers functionality to edit or delete them.

### Components and Logic

#### State Management

*   **`addAnnouncement`**: A Boolean state variable that controls the visibility of the add announcement form.
*   **`announcementAdmin`, `announcementRepository`, `userRepository`**: Instances used to access the respective repositories from the application context.
*   **`announcementViewModel`, `userViewModel`**: ViewModel instances created using factories to manage the state and business logic related to announcements and users.
*   **`announcements`, `announcementsLoading`**: Observed states from `announcementViewModel` that represent the list of announcements and the loading status, respectively.
*   **`refresh`**: A Boolean state that triggers the fetching of announcements when its value changes.
*   **`editingAnnouncementId`**: A nullable String state variable that keeps track of the announcement currently being edited.

#### Effect and Data Fetching

*   **`LaunchedEffect`**: This effect is triggered when the `refresh` state changes. It calls the `fetchAnnouncements()` function from the `announcementViewModel` to load the announcements.

#### Scaffold and Top App Bar

*   **`Scaffold`**: Provides the basic structure for the screen, including a `TopAppBar`.
*   **`TopAppBar`**: Contains two `IconButton` components: one for adding a new announcement and another for refreshing the announcements list. Clicking the add button toggles the visibility of the add announcement form, while the refresh button initiates data re-fetching.

#### Content

*   **`Column` and `Row` Layouts**: A `Column` is used to arrange the content vertically, with a `Row` employed to display the screen title centered at the top.
*   **`Text` and `Spacer`**: The `Text` composable displays the "Announcements" title and a subtitle for user guidance. The `Spacer` adds vertical spacing between components.

#### AnimatedVisibility

*   This composable controls the visibility of the `AddAnnouncement` composable based on the `addAnnouncement` state.

#### Loading and Empty State

*   Conditional rendering is used based on the `announcementsLoading` and `announcements` states:
  *   If `announcementsLoading` is true, a `CircularProgressIndicator` is displayed.
  *   If `announcements` is empty or null, a message indicating that there are no announcements is shown.

#### LazyColumn

*   The list of announcements is displayed using a `LazyColumn`.
*   The list items are sorted in descending order by ID before being rendered.
*   Each announcement is presented within an `AnnouncementCard`, which provides options to edit or delete the announcement:
  *   **Edit**: Toggles the editing state for the specific announcement.
  *   **Delete**: Triggers the `deleteAnnouncement` function in `announcementViewModel` and displays a Toast message if the deletion fails.

#### AnnouncementCard

*   This composable represents each announcement in the list and includes logic to handle editing and deleting operations.

## Visuals

### Announcements
![Announcements](Images/Announcements.png)

### Announcements Add
![Announcements Add](Images/AnnouncementsAdd.png)

### Announcements Edit
![Announcements Edit](Images/AnnouncementsEdit.png)


The `AnnouncementsScreen` presents a visually appealing and user-friendly interface for managing announcements. The top app bar provides easy access to actions for adding new announcements and refreshing the list. The announcements are displayed in a clear and organized manner within the `LazyColumn`, with each `AnnouncementCard` offering options for editing or deleting. Loading states and potential empty states are handled gracefully, providing appropriate feedback to the user.

### Important Notes

*   The function uses `remember` and `mutableStateOf` to manage state variables.
*   It handles loading states and provides visual feedback to the user.
*   It uses Toast messages to inform the user about the status of operations, such as successful deletion.

By leveraging these components and logic, the `AnnouncementsScreen` provides a robust and intuitive experience for managing announcements within the Uni Admin.
