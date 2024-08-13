package com.mike.uniadmin

//export database using this command
//adb shell "run-as com.mike.uniadmin cp databases/UniAdminDatabase /storage/emulated/0/Download/ 2>&1; echo \$?" && adb pull /storage/emulated/0/Download/UniAdminDatabase



//ISSUES TO CONSIDER
//Add a section in the account deletion status to tell if a user has account deletion request ✅
//work on the dark theme settings, the state should be correct✅
//add alerts part that will show notifications✅
//work on the github profile icon✅
//download new font styles✅
//work on the remaining bottom navigation items (timetable, assignments and the attendance✅)
//the attendance here will be used to check the attendance of each student
//implementing the Biometrics✅

//SCREENS TO ADD
//A screen for managing accounts such as deletion, adding, editing✅
//A screen for managing courses(ADDING, EDITING AND DELETING)
//A screen for statistics (IMPORTED FROM UNI KONNECT)
//A screen for sending alert messages to users like upgrade needed, managing the app version



//ADDITIONAL OPTIONAL FEATURES
//feature for enabling offline mode❌
//feature for liking announcements
//feature for showing previous feedback of a current signed in user



//BUGS AND IMPROVEMENTS TO MAKE

//LIGHT THEME
//the light theme is giving Zimbabwe //later

//HOME SCREEN
//the modal drawer is blinking then disappearing ✅
//the profile icon is showing null at start before loading the image ✅
//the timetable is null initially, which requires you to
// open the course content for it to load✅

//GROUP CHAT SCREEN
//the users list to have some padding both on top and bottom✅
//the animated to appear and disappear only from one direction✅
//fix the date display, there is wrong date title✅

//USER CHAT LIST
//the chats are not opening on time when you click on them✅

//ANNOUNCEMENTS SCREEN
//fix the text fields for new assignments (imepadding()) needed ✅

//COURSE CONTENT SCREEN
//add scrollable column property to display all the course details content✅
//the course name is not displaying properly✅


//Data encryption
//the user to user messages is yet to be encrypted, its halfway done
//the QR code scanning and generation is done
//only the implementation remaining.
//before opening a chat, we should check our file if it has the key in shared preferences on the device. after that we should open the chat
//more tomorrow