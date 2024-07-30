*UNI ADMIN*

GoogleAuth
Functionality
The GoogleAuth composable function handles Google authentication using Firebase. It initiates the Google sign-in process and provides visual feedback to the user.

Parameters
firebaseAuth: An instance of FirebaseAuth used to initiate the Google sign-in process.
onSignInSuccess: A callback function triggered when the sign-in process is successful.
onSignInFailure: A callback function triggered when the sign-in process fails, passing an error message.
Behavior
When the box is clicked, the function starts the Google sign-in process using Firebase Authentication.
While the sign-in process is ongoing, a circular progress indicator is displayed.
If the sign-in is successful, a check icon is displayed and onSignInSuccess is called.
If the sign-in fails, an error message is displayed and onSignInFailure is called.