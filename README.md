# ToolTrail
ToolTrail - Shared Equipment Borrowing and Return Tracker
BSE 3340 Mobile Application Development, Zambia University College of Technology
Repository: https://github.com/THFCMAZ01/ToolTrail
Demo video (YouTube, Unlisted): [add link after upload]
1. The problem
Small organisations such as student clubs, churches, community groups and workshops often lend out shared equipment. Without a simple record it is hard to know who borrowed an item, when it is due back, and which items have not been returned. This leads to lost equipment, delays and misunderstandings.
2. Who it is for
The person who looks after shared equipment in a small organisation and needs a simple offline record of what is out, who has it and when it is due.
3. What the app does
Register equipment (name, category, description, condition)
Show all equipment, sorted by name, with its status: Available or On loan
Record who borrowed an item, their contact and when it is due back
Prevent the same item being lent to two people at once
Show active loans, soonest due first, with an OVERDUE label when the due time has passed
Mark an item as returned
Keep a history of completed loans
Show dashboard totals: total equipment, available, active loans, overdue
Send a notification when a loan becomes due
The app works fully offline. All records are stored on the device.
4. Screens
Screen
What it does
Dashboard
Four counters: total equipment, available, active loans, overdue. Shows a hint when there is no equipment yet.
Equipment
Scrollable list. Each card shows name, category, condition, description and status, with a Borrow button (disabled while on loan). A + button opens the Add screen.
Add equipment
Form with validation: name and category are required.
Borrow equipment
Form with validation: borrower name, contact and hours until return are required. Hours must be a number greater than 0.
Loans
Active loans with borrower, contact, due date, an OVERDUE label and a Mark returned button.
History
Completed loans with borrowed and returned times.
Equipment details are shown on the Equipment list cards. There is no separate details screen.
Navigation and back button: the Dashboard, Equipment, Loans and History screens are reached from the bottom bar. Back from Add equipment or Borrow equipment returns to the Equipment list. Back from the other tabs returns to the Dashboard.
Empty states: the Equipment, Loans and History screens each show a message when they have no data.
5. Project structure
app/src/main/java/com/zict/tooltrail/
  MainActivity.kt                 entry point, asks for notification permission
  data/entity/                    EquipmentEntity, BorrowingEntity (table definitions)
  data/dao/                       EquipmentDao, BorrowingDao (database queries)
  data/database/                  ToolTrailDatabase (the Room database)
  data/repository/                ToolTrailRepository (validation and business rules)
  ui/                             ToolTrailViewModel, ToolTrailApp (screens)
  notifications/                  ReminderWorker, ReminderScheduler
How the layers connect: screens (ToolTrailApp) read state from the ViewModel. The ViewModel calls the repository. The repository checks the rules and calls the DAOs. The DAOs run queries on the Room database. Screens never touch the database directly.
Data model:
equipment table: id, name, category, description, condition, dateAdded
borrowings table: id, equipmentId (foreign key to equipment), borrowerName, borrowerContact, borrowedAt, dueDate, returnedAt
returnedAt is null while the item is out. When the item is returned it is filled with the return time. This lets the app tell active loans from history without deleting any records.
The foreign key uses RESTRICT, so equipment that has loan records cannot be deleted.
6. Lifecycle, validation and saving data
Rotation and leaving the app. The ViewModel keeps the screen state and survives rotation. The current screen, the selected item and text typed into forms are kept with rememberSaveable. Flows from Room are collected with collectAsStateWithLifecycle, so collection pauses when the app is in the background.
Saving data. Every record is stored in a Room (SQLite) database named tooltrail_database, so it remains after the app is closed. Note that uninstalling or reinstalling the app clears it.
Validation. Forms show an error under the field when it is empty or invalid. The repository checks the same rules again (blank name, category, borrower or contact, due date not in the future, item not found, item already borrowed, loan already returned) and any error is shown in a Snackbar message.
7. Technology
Kotlin, Jetpack Compose with Material 3, Room (with KSP), Kotlin Coroutines and Flow, ViewModel, WorkManager. Minimum SDK 24, target and compile SDK 37, package com.zict.tooltrail.
8. How to build and run
Install a recent version of Android Studio with the Android SDK Platform 37.
Clone the repository: git clone https://github.com/THFCMAZ01/ToolTrail.git
Open the cloned folder in Android Studio and wait for the Gradle sync to finish.
Choose an emulator or a phone with USB debugging (Android 7.0 / API 24 or higher) and press Run.
On Android 13 or higher, tap Allow when the app asks to send notifications.
Command line (Windows): .\gradlew.bat --no-configuration-cache :app:assembleDebug
The build prints a warning that android.disallowKotlinSourceSets is experimental and a message that Kotlin does not support JDK 25 and falls back to JVM 24. Both are expected and the build still succeeds.
Quick demo of a reminder: borrow an item with 0.02 hours (about one minute), press Home, and wait one to two minutes.
9. Research Note
Concept 1: Room database (owner: Moses Phiri)
What it is. Room is an Android library that sits on top of SQLite. We describe tables as Kotlin classes (entities), write queries as interface methods (DAOs), and Room generates the database code at build time. In ToolTrail the entities are EquipmentEntity and BorrowingEntity, the DAOs are EquipmentDao and BorrowingDao, and ToolTrailDatabase ties them together as one shared instance.
Why ToolTrail needs it. Equipment and loan records must survive closing the app, and the screens must update when data changes. Room queries return Kotlin Flow, so the lists on screen refresh automatically after an insert or an update.
Where we learned it. The Android Developers documentation on Room (developer.android.com/training/data-storage/room), and Claude as an AI tutor and code assistant.
What went wrong.
The KSP plugin conflicted with the Android Gradle Plugin's built-in Kotlin support. The build only worked after adding android.disallowKotlinSourceSets=false to gradle.properties. The build still prints a warning that this option is experimental.
While writing the return feature we found that BorrowingDao had no query to fetch one loan by its ID, so we added getBorrowingById.
After reinstalling the app on the phone, all data was gone, because a fresh install creates a new empty database.
Concept 2: Notifications and WorkManager (owner: Moses Phiri)
What it is. WorkManager schedules background work that still runs if the app is closed or the phone restarts. A notification is how the app tells the user something while it is not on screen.
Why ToolTrail needs it. A borrower should be reminded when equipment is due. When a loan is created, the ViewModel asks ReminderScheduler to queue a one-time job with a delay equal to the time until the due date. When the item is returned, the job is cancelled. When the job runs, ReminderWorker checks the database, and posts a notification only if the loan has not been returned.
Where we learned it. The Android Developers documentation on WorkManager (developer.android.com/develop/background-work/background-tasks/persistent/getting-started) and on notifications (developer.android.com/develop/ui/views/notifications), and Claude as an AI tutor and code assistant.
What went wrong.
From Android 13 an app must have the notification permission. During testing no notification appeared until the permission was granted on the phone.
The first build did not show the permission prompt because the old version of MainActivity was still installed; the prompt appeared after reinstalling the new build.
WorkManager does not fire at the exact second, so the notification appears a short time after the due time.
AI tools used
We used Claude as a tutor and code assistant for explanations and code. We checked each step by building and testing on a real Samsung phone.
10. Testing
We tested on a real Samsung phone (Android API 37) after a fresh install. All of these passed:
Saving the Add equipment form with blank fields shows the required-field errors.
A new item appears in the list as Available.
Borrowing an item moves the app to the Loans screen with the loan listed.
The item shows On loan with Borrow disabled, and the Dashboard counts update.
Mark returned removes the loan from Loans and adds it to History, and the item is Available again.
Text typed in a form survives rotating the phone.
Data is still there after swiping the app away and reopening it.
Returning an item before it is due cancels its reminder, so no notification appears.
A reminder notification appears after a short loan (0.02 hours) has passed.
11. Known limitations
There is no separate equipment details screen. Details are shown on the list cards.
The return time is entered as hours from now. It is shown as a date and time in the app.
The reminder fires when the loan becomes due. There is no earlier warning.
If notification permission is denied, the app works normally but shows no reminders and no message about it.
The database is version 1 with no migrations. Changing a table needs a new database version.
12. Team
Member
Student number
Role
Innocent Kabwe
2410109
Technical lead: setup, navigation and integration
Joshua Mazaza
2410123
Coordination, code review and quality assurance
Gabriel Mwila
2410234
Equipment management
Ndame Sepete
2410559
Borrowing and loan management
Moses Phiri
2410103
Database and reminders